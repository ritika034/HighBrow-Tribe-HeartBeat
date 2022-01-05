package com.example.Tribes.Repo;

import com.example.Tribes.Model.TribeQuestionDetails;
import com.example.Tribes.Model.User;
import org.springframework.data.repository.CrudRepository;

public interface TribeQuestionRepo extends CrudRepository<TribeQuestionDetails,Long> {
}
