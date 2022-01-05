package service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import service.centralCore.UserInfo;
import service.messages.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Communicator extends AbstractActor {
    private static ActorSystem system;
    private static ActorSelection persistanceActor;
    public static void main(String [] args){

        system = ActorSystem.create();
        system.actorOf(Props.create(Communicator.class), "communicator");
        persistanceActor = system.actorSelection("akka.tcp://default@127.0.0.1:2552/user/userSystem");

    }
    private ActorSelection TriberActor = system.actorSelection("akka.tcp://default@127.0.0.1:2557/user/triber");
    //private HashMap<Long, Tribe> ActiveUsers = new HashMap<>();
    private static HashMap<String, Long> gitHubIdRequestId = new HashMap<>();
    private static HashMap<Long, List<UserInfo>> ActiveUsers = new HashMap<>();
    private static HashMap<Long, Integer> UserPorts = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProblemSolvedResponse.class,
                        msg -> {
                            ActorSelection selection;
                            for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                                long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                                System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                                selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);
                                selection.tell(new ChatMessageReceive("Bot : ", new Timestamp(System.currentTimeMillis()), msg.getUniqueId(), msg.getErrorMessage()), null);
                            }
                        })
                .match(ChatRegisterRequest.class,
                        msg -> {
                            long tribeId = msg.getUserInfo().getTribeId();
                            long uniqueId = msg.getUniqueId();
                            System.out.println("Chat register request received from User : "+msg.getUserInfo().getName()+" Unique ID: " + msg.getUniqueId());
                            gitHubIdRequestId.put(msg.getUserInfo().getGitHubId(),uniqueId);
                            ActiveUsers.put(tribeId,null);
                            UserPorts.put(uniqueId, msg.getUserInfo().getPortNumber());
                            TriberActor.tell(new TribeDetailRequest(uniqueId,tribeId), getSelf());
                        })
                .match(TribeDetailResponse.class,
                        msg -> {
                            ActiveUsers.put(msg.getTribe().getTribeId(),msg.getTribe().getMembers());
                            int PortNumber = UserPorts.get(msg.getUniqueId());
                            ActorSelection clientActor = system.actorSelection("akka.tcp://default@127.0.0.1:" + PortNumber + "/user/" + msg.getUniqueId());
                            clientActor.tell(new ChatRegisterResponse(msg.getTribe()), null);
                        })
                .match(ChatMessageSend.class,
                        msg -> {
                            ActorSelection selection;
                            if(msg.getMessage().equals("!problem question")){
                              //Make call to database to fetch question for the tribe
                              persistanceActor.tell(new FetchQuestionForTribeRequest(msg.getUniqueId(),msg.getTribeId()),getSelf());
                            }
                            else if(msg.getMessage().equals("!problem solved")){
                              persistanceActor.tell(new TribeDetailRequest(msg.getUniqueId(),msg.getTribeId())
                                        ,getSelf());
                            }
                            else {
                                for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                                    long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                                    if (uniqueId != msg.getUniqueId()) {
                                        System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                                        selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);
                                        selection.tell(new ChatMessageReceive(msg.getSenderName(), msg.getSentTime(), msg.getUniqueId(), msg.getMessage()), null);
                                    }
                                }
                            }
                        })
                .match(FetchQuestionForTribeResponse.class, msg->{
                    for (UserInfo user : ActiveUsers.get(msg.getTribeId())) {
                        long uniqueId = gitHubIdRequestId.get(user.getGitHubId());
                        if (uniqueId == msg.getUniqueId()) {
                            System.out.println("PortNumber : " + user.getPortNumber() + " Unique Id : " + uniqueId);
                            ActorSelection selection = system.actorSelection("akka.tcp://default@127.0.0.1:" + user.getPortNumber() + "/user/" + uniqueId);
                            selection.tell(new ChatMessageReceive("Bot : ", new Timestamp(System.currentTimeMillis()), msg.getUniqueId(), msg.getQuestion()), null);
                        }
                    }
                })
                .match(ChatMessageReceive.class,
                        msg -> {
//                            ActiveUsers.put(msg.getUniqueId(), msg.getTribe());
                        }).build();
    }
}
