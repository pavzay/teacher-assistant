package com.grsu.teacherassistant.beans;

import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.entities.*;
import com.grsu.teacherassistant.models.LessonType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static com.grsu.teacherassistant.utils.FacesUtils.closeDialog;
import static com.grsu.teacherassistant.utils.FacesUtils.update;

/**
 * Created by pavel on 3/10/17.
 */
@ManagedBean(name = "newLessonBean")
@ViewScoped
public class NewLessonBean implements Serializable {
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;

	private Lesson lesson;

	private List<LessonType> lessonTypes = new ArrayList<>(Arrays.asList(LessonType.LECTURE, LessonType.PRACTICAL, LessonType.LAB, LessonType.EXAM));

	private void initLesson() {
		lesson.setDate(LocalDateTime.now());
	}

	public void exit() {
//		sessionBean.setActiveView("lessons");
		clear();
		update("views");
		closeDialog("lessonDialog");
	}

	private void clear() {
		lesson = null;
	}

	public void createLesson() {
		if (lesson != null) {
			if (lesson.getType() == null || lesson.getType() == LessonType.LECTURE) {
				lesson.setGroup(null);
			}

			EntityDAO.add(lesson);

			List<Group> groups;

			if (lesson.getGroup() == null) {
				groups = lesson.getStream().getGroups();
			} else {
				groups = Arrays.asList(lesson.getGroup());
			}

			Set<Student> students = new HashSet<>();
			for (Group group : groups) {
				students.addAll(group.getStudents());
			}

			List<StudentLesson> studentLessons = new ArrayList<>();
			for (Student student : students) {
				StudentLesson sc = new StudentLesson();
				sc.setStudent(student);
				sc.setLesson(lesson);
				studentLessons.add(sc);
			}
			EntityDAO.add(new ArrayList<>(studentLessons));

			sessionBean.updateStudents();
		}
		exit();
	}

	/*GETTERS AND SETTERS*/
	public void setLesson(Lesson lesson) {
		this.lesson = lesson;
		initLesson();
	}

	public Lesson getLesson() {
		return lesson;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public List<LessonType> getLessonTypes() {
		return lessonTypes;
	}
}
