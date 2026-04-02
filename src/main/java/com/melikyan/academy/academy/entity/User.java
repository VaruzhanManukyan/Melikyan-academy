package com.melikyan.academy.academy.entity;

import com.melikyan.academy.academy.entity.base.BaseEntitySoftDelete;
import com.melikyan.academy.academy.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<UserProcess> userProcesses;

    @OneToMany(mappedBy = "createdBy")
    private List<Category> categories;

    @OneToMany(mappedBy = "createdBy")
    private List<Purchasable> purchasables;

    @OneToMany(mappedBy = "createdBy")
    private List<Product> products;

    @OneToMany(mappedBy = "user")
    private List<Professor> professors;

    @OneToMany(mappedBy = "user")
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "user")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    private List<ProductRegistration> productRegistrations;

    @OneToMany(mappedBy = "createdBy")
    private List<Language> languages;

    @OneToMany(mappedBy = "createdBy")
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "createdBy")
    private List<Homework> homework;

    @OneToMany(mappedBy = "createdBy")
    private List<HomeworkTask> homeworkTasks;

    @OneToMany(mappedBy = "user")
    private List<HomeworkSubmission> homeworkSubmissions;

    @OneToMany(mappedBy = "user")
    private List<LessonAttendance> lessonAttendances;

    @OneToMany(mappedBy = "createdBy")
    private List<ExamSection> examSections;

    @OneToMany(mappedBy = "createdBy")
    private List<ExamTask> examTasks;

    @OneToMany(mappedBy = "user")
    private List<ExamSubmission> examSubmissions;

    @OneToMany(mappedBy = "createdBy")
    private List<PurchasableTranslation> purchasableTranslations;

    @OneToMany(mappedBy = "createdBy")
    private List<SectionTranslation> sectionTranslations;

    @OneToMany(mappedBy = "createdBy")
    private List<ProductTranslation> productTranslations;

    @OneToMany(mappedBy = "createdBy")
    private List<LessonTranslation> lessonTranslations;

    @OneToMany(mappedBy = "createdBy")
    private List<HomeworkTranslation> homeworkTranslations;
}
