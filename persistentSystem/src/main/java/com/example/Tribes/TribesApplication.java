package com.example.Tribes;

import com.example.Tribes.Model.Questions;
import com.example.Tribes.Model.TribeQuestionDetails;
import com.example.Tribes.Model.User;
import com.example.Tribes.Repo.Constants;
import com.example.Tribes.Repo.QuestionsRepo;
import com.example.Tribes.Repo.TribeQuestionRepo;
import com.example.Tribes.Repo.UserRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.centralCore.*;
import service.messages.TriberInitializationResponse;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;


@SpringBootApplication
public class TribesApplication {

	public static void main(String[] args) {
		Constants.configurableApplicationContext = SpringApplication.run(TribesApplication.class, args);
	}

	public static ArrayList<Questions> getQuestion(){
		QuestionsRepo questionsRepo = Constants.configurableApplicationContext.getBean(QuestionsRepo.class);
		return (ArrayList<Questions>) questionsRepo.findAll();
	}

	public static TribeQuestionDetails getTribeQuestionDetails(long tribeId){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		return tribeQuestionRepo.findById(tribeId).orElse(null);
	}

	public static void deleteTribeQuestionDetails(final Long tribeId){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		tribeQuestionRepo.deleteById(tribeId);
	}

	public static void setTribeQuestionDetails(final Long tribeId, final String question){
		TribeQuestionRepo tribeQuestionRepo = Constants.configurableApplicationContext.getBean(TribeQuestionRepo.class);
		TribeQuestionDetails tribeQuestionDetails = new TribeQuestionDetails(tribeId,question,new Timestamp(System.currentTimeMillis()));
		tribeQuestionRepo.save(tribeQuestionDetails);
	}

	public static void setUserInfo(final Long uniqueId,final UserInfo userInfo,final String tribeLanguage){
		UserRepo userRepo = Constants.configurableApplicationContext.getBean(UserRepo.class);
		User user = new User(uniqueId,userInfo.getName(),userInfo.getTribeId(), userInfo.getInterests().getProgrammingLanguages().stream().collect(Collectors.joining(",")),userInfo.getGitHubId(),tribeLanguage,userInfo.getPortNumber());
		userRepo.save(user);
	}

	public static TriberInitializationResponse getAllUserInfo() {

		UserRepo userRepo = Constants.configurableApplicationContext.getBean(UserRepo.class);
		ArrayList<User> users = (ArrayList<User>) userRepo.findAll();
		ArrayList<UserInfo> allUsers = new ArrayList<>();
		users.forEach(user->{
			UserInfo mappedUserInfo = new UserInfo();
			mappedUserInfo.setName(user.getName());
			mappedUserInfo.setGitHubId(user.getGitHubId());
			mappedUserInfo.setPortNumber(user.getPortNumber());
			mappedUserInfo.setTribeId(user.getTribeId());
			allUsers.add(mappedUserInfo);
		});
		if (users.size() > 0) {
			ArrayList<Tribe> tribeArrayList = new ArrayList<>();
			Map<Long, List<User>> tribeAndUserMap = users.stream().collect(groupingBy(User::getTribeId));

			tribeAndUserMap.forEach((k, v) ->
			{
				List<UserInfo> userInfoListTemp = new ArrayList<>();
				v.forEach(item -> {
					UserInfo userInfo = new UserInfo(item.getName(), item.getGitHubId());
					userInfo.setPortNumber(item.getPortNumber());
					//userInfo.setUniqueId(item.getUniqueId());
					userInfo.setTribeId(item.getTribeId());
					//userInfo.setTribeLanguage(item.getTribeLanguage());
					userInfoListTemp.add(userInfo);

				});

				String tribeProgrammingLanguages= String.join(",",Stream.of(v.stream().map(User::getProgrammingLanguage)
								.collect(Collectors.joining(",")).trim().split("\\s*,\\s*"))
						.collect(Collectors.toSet()));

				Tribe t = new Tribe(k, v.get(0).getTribeLanguage(), tribeProgrammingLanguages, userInfoListTemp);
				tribeArrayList.add(t);
			});

			Long maxTribeId = users.stream().max(Comparator.comparing(User::getTribeId)).orElseThrow().getTribeId();
			Long maxUniqueId = users.stream().max(Comparator.comparing(User::getUniqueId)).orElseThrow().getUniqueId();

			return new TriberInitializationResponse(allUsers,
					tribeArrayList, maxUniqueId, maxTribeId);
		} else {
			return new TriberInitializationResponse(null,
					new ArrayList<Tribe>(), 0L, 0L);
		}
	}

}