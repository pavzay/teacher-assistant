package com.grsu.teacherassistant.beans.mode;

import com.grsu.teacherassistant.beans.utility.SerialBean;
import com.grsu.teacherassistant.beans.utility.SerialListenerBean;
import com.grsu.teacherassistant.beans.utility.SessionBean;
import com.grsu.teacherassistant.constants.Constants;
import com.grsu.teacherassistant.dao.EntityDAO;
import com.grsu.teacherassistant.entities.*;
import com.grsu.teacherassistant.entities.StudentLesson;
import com.grsu.teacherassistant.models.LessonStudentModel;
import com.grsu.teacherassistant.models.Mark;
import com.grsu.teacherassistant.utils.EntityUtils;
import com.grsu.teacherassistant.utils.FacesUtils;
import lombok.Data;
import org.primefaces.component.inputtext.InputText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Zaychick
 */
@ManagedBean(name = "studentModeBean")
@ViewScoped
@Data
public class StudentModeBean implements Serializable, SerialListenerBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentModeBean.class);

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{serialBean}")
    private SerialBean serialBean;

    private Stream stream;
    private LessonStudentModel lessonStudent;
    private Student student;

    private StudentLesson selectedStudentLesson;
    private String newNote;
    private boolean registered;
    private StudentLesson editedStudentLesson;

    private List<Stream> studentStreams;

    public void initStudentMode(Student student, Stream stream) {
        serialBean.setCurrentListener(this);
        clear();
        this.student = student;
        this.stream = stream;

        if (this.student != null) {
            lessonStudent = new LessonStudentModel(student, stream);
            studentStreams = student.getStudentLessons().values().stream()
                .filter(sl -> sl.getLesson() != null)
                .map(sl -> sl.getLesson().getStream())
                .distinct()
                .sorted((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()))
                .collect(Collectors.toList());
            if (stream == null && studentStreams.size() > 0) {
                this.stream = studentStreams.get(0);
            }

        }
    }

    public boolean isAdditionalLesson(Lesson lesson) {
        if (lesson.getGroup() == null) {
            for (Group group : lesson.getStream().getGroups()) {
                if (lessonStudent.getStudent().getGroups().contains(group)) {
                    return false;
                }
            }
        }
        if (lessonStudent.getStudent().getGroups().contains(lesson.getGroup())) {
            return false;
        }
        return true;
    }

    public void clear() {
        stream = null;
        lessonStudent = null;

        selectedStudentLesson = null;
        newNote = null;

        studentStreams = null;

    }

    public List<Map.Entry<Integer, Integer>> getNumberMarks() {
        if (lessonStudent.getNumberMarks() != null) {
            return new ArrayList<>(lessonStudent.getNumberMarks().entrySet());
        }
        return null;
    }

    public List<Map.Entry<String, Integer>> getSymbolMarks() {
        if (lessonStudent.getSymbolMarks() != null) {
            return new ArrayList<>(lessonStudent.getSymbolMarks().entrySet());
        }
        return null;
    }

    public void changeExamMark(ValueChangeEvent event) {
        if (event.getSource() instanceof InputText && "examMark".equals(((InputText) event.getSource()).getId())) {
            lessonStudent.setExamMark((Mark) event.getNewValue());
            lessonStudent.updateTotal();
        }
        if (event.getSource() instanceof InputText && "totalMark".equals(((InputText) event.getSource()).getId())) {
            lessonStudent.setTotalMark((Mark) event.getNewValue());
            lessonStudent.updateExam();
        }
        lessonStudent.saveExam();
    }

    public void changeAttestationMark(ValueChangeEvent event) {
        int attestationId = Integer.parseInt(String.valueOf(event.getComponent().getAttributes().get("attestationId")));
        lessonStudent.updateAttestationMark(attestationId, (Mark) event.getNewValue());
    }

    public void editMark(StudentLesson studentLesson) {
        editedStudentLesson = studentLesson;
    }

    public void saveMark(ValueChangeEvent event) {
        if (event != null) {
            String value = String.valueOf(event.getNewValue());
            value = value != null ? (value.trim().isEmpty() ? null : value.trim()) : null;
            editedStudentLesson.setMark(value);
            EntityDAO.save(editedStudentLesson);
            lessonStudent.initMarks();
        }
        editedStudentLesson = null;
    }

    //NOTES
    public void saveNote() {
        if (newNote != null && !newNote.isEmpty()) {
            Note note = new Note();
            note.setCreateDate(LocalDateTime.now());
            note.setDescription(newNote);
            note.setType(Constants.STUDENT_LESSON);
            note.setEntityId(selectedStudentLesson.getId());
            EntityDAO.save(note);
            selectedStudentLesson.getNotes().add(note);
        }
        newNote = null;
        FacesUtils.closeDialog("notesDialog");
    }

    public void removeNote(Note note) {
        EntityDAO.delete(note);
        selectedStudentLesson.getNotes().remove(note);
    }

    //REGISTERED INFO
    public void saveRegisteredInfo() {
        boolean oldValue = selectedStudentLesson.isRegistered();
        if (oldValue != registered) {
            selectedStudentLesson.setRegistered(registered);
            if (!registered) {
                selectedStudentLesson.setRegistrationTime(null);
                selectedStudentLesson.setRegistrationType(null);
            } else {
                selectedStudentLesson.setRegistrationTime(LocalTime.now());
                selectedStudentLesson.setRegistrationType(Constants.REGISTRATION_TYPE_MANUAL);
            }
            EntityDAO.save(selectedStudentLesson);
            lessonStudent.updateSkips(stream);
        }
        FacesUtils.closeDialog("registeredDialog");
    }

    @Override
    public boolean process(String uid, String name) {
        Student student = EntityUtils.getPersonByUid(sessionBean.getStudents(), uid);
        if (student != null) {
            initStudentMode(student, null);

            FacesUtils.push("/register", uid);
            return true;
        } else {
            LOGGER.info("Student not registered. Reason: Uid[ " + uid + " ] not exist in database.");
            return false;
        }
    }
}
