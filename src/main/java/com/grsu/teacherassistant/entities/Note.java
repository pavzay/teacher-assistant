package com.grsu.teacherassistant.entities;

import com.grsu.teacherassistant.converters.db.LocalDateTimeAttributeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Pavel Zaychick
 */
@Entity
@Getter
@Setter
public class Note implements AssistantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic
    @Column(name = "type")
    private String type;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "entity_id")
    private Integer entityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (id != null ? !id.equals(note.id) : note.id != null) return false;
        if (type != null ? !type.equals(note.type) : note.type != null) return false;
        if (description != null ? !description.equals(note.description) : note.description != null) return false;
        if (createDate != null ? !createDate.equals(note.createDate) : note.createDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Note{" +
            "id=" + id +
            ", type='" + type + '\'' +
            ", description='" + description + '\'' +
            ", createDate=" + createDate +
            ", entityId=" + entityId +
            '}';
    }
}
