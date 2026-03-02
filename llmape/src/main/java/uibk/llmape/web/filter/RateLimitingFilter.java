package uibk.llmape.web.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uibk.llmape.config.ApplicationProperties;
import uibk.llmape.service.MailService;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
@Order(1)
@Profile("!test")
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final MailService mailService;
    private final ApplicationProperties applicationProperties;
    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(100000)
        .build();

    private final Bucket globalBucket;
    private Instant perIpBucketExceededMailLastSent = Instant.MIN;
    private Instant globalBucketExceededMailLastSent = Instant.MIN;
    private Instant globalBucketHalfExceededMailLastSent = Instant.MIN;

    public RateLimitingFilter(MailService mailService, ApplicationProperties applicationProperties){
        this.mailService = mailService;
        this.applicationProperties = applicationProperties;
        this.globalBucket = Bucket.builder().addLimit(limit -> limit.capacity(applicationProperties.getRateLimit().getGlobal()).refillGreedy(applicationProperties.getRateLimit().getGlobal(), Duration.ofHours(1))).build();
    }

    private Bucket resolveBucket(String ip){
        return ipBuckets.get(ip, key -> newBucket());
    }

    private Bucket newBucket(){
        return Bucket.builder().addLimit(limit -> limit.capacity(applicationProperties.getRateLimit().getPerIp()).refillGreedy(applicationProperties.getRateLimit().getPerIp(), Duration.ofHours(1))).build();
    }

    private String getClientIP(HttpServletRequest request){
        String xfHeader = request.getHeader("X-Forwarded-For");
        if(xfHeader != null){
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var path = request.getRequestURI();
        var ip = getClientIP(request);

        if(!path.startsWith("/api/myprompt") || path.startsWith("/api/myprompt/generatePromptAndVote")){
            filterChain.doFilter(request, response);
            return;
        }

        var ipBucket = resolveBucket(ip);

        if(!ipBucket.tryConsume(1)){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded, please try again later!");
            LOG.warn("Per-IP rate limit exceeded. IP: {}", ip);
            if(perIpBucketExceededMailLastSent.isBefore(Instant.now().minus(Duration.ofHours(1)))){
                mailService.sendRateLimitExceededMail(false);
                perIpBucketExceededMailLastSent = Instant.now();
            }
            return;
        }

        if(!globalBucket.tryConsume(1)){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded, please try again later!");
            LOG.warn("Global rate limit exceeded.");
            if(globalBucketExceededMailLastSent.isBefore(Instant.now().minus(Duration.ofHours(1)))){
                mailService.sendRateLimitExceededMail(true);
                globalBucketExceededMailLastSent = Instant.now();
            }
            return;
        }

        if(globalBucket.getAvailableTokens() < applicationProperties.getRateLimit().getGlobal()/2){
            if(globalBucketHalfExceededMailLastSent.isBefore(Instant.now().minus(Duration.ofHours(1)))){
                mailService.sendRateLimitHalfExceededMail(true);
                globalBucketHalfExceededMailLastSent = Instant.now();
            }
        }

        filterChain.doFilter(request, response);
    }
}
