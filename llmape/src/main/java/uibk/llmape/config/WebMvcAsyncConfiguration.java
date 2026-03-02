package uibk.llmape.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAsyncConfiguration implements WebMvcConfigurer {

	private final Executor mvcExecutor;

	public WebMvcAsyncConfiguration(@Qualifier("taskExecutor") Executor mvcExecutor) {
		this.mvcExecutor = mvcExecutor;
	}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		configurer.setTaskExecutor((AsyncTaskExecutor) mvcExecutor);
		configurer.setDefaultTimeout(60_000L); // 60s timeout for async requests
	}
}
