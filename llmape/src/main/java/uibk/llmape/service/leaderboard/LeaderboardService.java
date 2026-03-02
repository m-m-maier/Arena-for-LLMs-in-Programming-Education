package uibk.llmape.service.leaderboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uibk.llmape.domain.LeaderboardEntry;
import uibk.llmape.domain.enumeration.Category;
import uibk.llmape.repository.BattleRepository;
import uibk.llmape.repository.LeaderboardEntryRepository;

@Service
public class LeaderboardService {

	private final BattleRepository battleRepository;
	private final LeaderboardEntryRepository leaderboardEntryRepository;
	private final ObjectMapper objectMapper;

	@Autowired
	public LeaderboardService(
		BattleRepository battleRepository,
		LeaderboardEntryRepository leaderboardEntryRepository,
		ObjectMapper objectMapper
	) {
		this.battleRepository = battleRepository;
		this.leaderboardEntryRepository = leaderboardEntryRepository;
		this.objectMapper = objectMapper;
	}

	public LeaderboardDTO getLeaderboard(Category category) throws JsonProcessingException {
		var leaderboardRowDtoList = this.battleRepository.getLeaderboardEntriesByCategory(category);
		var optionalLeaderboardEntry = category != null
			? leaderboardEntryRepository.findFirstByCategoryOrderByTimestampDesc(category)
			: leaderboardEntryRepository.findFirstByCategoryIsNullOrderByTimestampDesc();

		LeaderboardEntry leaderboardEntry = optionalLeaderboardEntry.orElse(null);

		if (leaderboardEntry == null) {
			return new LeaderboardDTO(leaderboardRowDtoList, null);
		}

		try {
			List<LeaderboardEntryDTO> leaderboardEntryDtoList = objectMapper.readValue(
				leaderboardEntry.getEntryJson(),
				new TypeReference<List<LeaderboardEntryDTO>>() {}
			);

			for (var row : leaderboardRowDtoList) {
				for (var entry : leaderboardEntryDtoList) {
					if (row.getId().equals(entry.getId())) {
						row.setScore(entry.getScore());
					}
				}
			}
			return new LeaderboardDTO(leaderboardRowDtoList, leaderboardEntry.getTimestamp());
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse leaderboard data", e);
		}
	}
}
