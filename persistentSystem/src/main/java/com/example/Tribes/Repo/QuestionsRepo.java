package com.example.Tribes.Repo;

import com.example.Tribes.Model.Questions;
import com.example.Tribes.Model.TribeQuestionDetails;
import org.springframework.data.repository.CrudRepository;

public interface QuestionsRepo extends CrudRepository<Questions,Long> {
}
