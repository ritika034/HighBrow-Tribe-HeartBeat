package com.example.Tribes.service.actors;

import akka.actor.AbstractActor;
import com.example.Tribes.Repo.Constants;
import com.example.Tribes.TribesApplication;
import scala.concurrent.java8.FuturesConvertersImpl;
import service.messages.*;

import java.util.Random;

public class Persistent extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserCreationRequest.class,
                msg -> {
                    if(Constants.configurableApplicationContext == null) {
                        System.out.println("Inside If...");
                        TribesApplication.main(new String[0]);
                    }
                    TribesApplication.setUserInfo(msg.getUniqueId(),msg.getNewUser(),msg.getTribeLanguage());
                    System.out.println("Send user creation response to the triber system..For Unique ID: "+msg.getUniqueId()+" Port: "
                    +msg.getNewUser().getPortNumber()+" Tribe Language="+msg.getTribeLanguage());

                    UserCreationResponse userCreationResponse = new UserCreationResponse();
                    userCreationResponse.setUniqueId(msg.getUniqueId());
                    userCreationResponse.setTribeId(msg.getNewUser().getTribeId());
                    getSender().tell(userCreationResponse,self());
                })
                .match(FetchQuestionForTribeRequest.class, msg->{
                    String question = "";
                    FetchQuestionForTribeResponse fetchQuestionForTribeResponse;
                    if(TribesApplication.getTribeQuestionDetails(msg.getTribeId()) != null){
                        question = TribesApplication.getTribeQuestionDetails(msg.getTribeId()).getQuestion();
                        System.out.println("Question in if: ="+question);
                        fetchQuestionForTribeResponse = new FetchQuestionForTribeResponse(msg.getUniqueId(),
                                msg.getTribeId(), question);
                    }
                    else{
                        int index = new Random().nextInt(5);
                        question = TribesApplication.getQuestion().get(index).getQuestion();
                        System.out.println("Question in else: ="+question);
                        fetchQuestionForTribeResponse = new FetchQuestionForTribeResponse(msg.getUniqueId(),
                                msg.getTribeId(), question);
                        TribesApplication.setTribeQuestionDetails(msg.getTribeId(), question);
                    }
                    getSender().tell(fetchQuestionForTribeResponse, self());
                })
                .match(TribeDetailRequest.class,msg->{
                    ProblemSolvedResponse problemSolvedResponse = new ProblemSolvedResponse(msg.getUniqueId(),msg.getTribeId());
                    if(TribesApplication.getTribeQuestionDetails(msg.getTribeId()) != null){
                        TribesApplication.deleteTribeQuestionDetails(msg.getTribeId());
                        problemSolvedResponse.setErrorMessage("Congratulations on completing the challenge, You may now request a new challenge by entering !problem question");
                    }
                    else{
                        problemSolvedResponse.setErrorMessage("No challenge associated with the tribe, Please request a challenge by entering !problem question");
                    }
                    getSender().tell(problemSolvedResponse, self());
                })
                .match(String.class,
                msg -> {

                    if(Constants.configurableApplicationContext == null) {
                        TribesApplication.main(new String[0]);
                    }
                    if(msg.equals("InitializeTriberSystem")){
                        getSender().tell(TribesApplication.getAllUserInfo(),self());
                    }
                }).build();
    }
}