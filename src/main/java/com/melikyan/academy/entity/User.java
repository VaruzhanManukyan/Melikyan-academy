package com.melikyan.academy.entity;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntitySoftDelete {
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<UserProcess> userProcesses;

    @OneToMany(mappedBy = "user")
    private List<Professor> professors;

    @OneToMany(mappedBy = "user")
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "user")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    private List<ProductRegistration> productRegistrations;

    @OneToMany(mappedBy = "user")
    private List<HomeworkSubmission> homeworkSubmissions;

    @OneToMany(mappedBy = "user")
    private List<LessonAttendance> lessonAttendances;

    @OneToMany(mappedBy = "user")
    private List<ExamSubmission> examSubmissions;
}
