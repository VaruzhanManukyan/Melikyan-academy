package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.entity.Purchasable;
import com.melikyan.academy.mapper.CourseMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.entity.enums.PurchasableType;
import com.melikyan.academy.repository.CategoryRepository;
import com.melikyan.academy.repository.PurchasableRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.course.CourseResponse;
import com.melikyan.academy.dto.request.course.UpdateCourseRequest;
import com.melikyan.academy.dto.request.course.CreateCourseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final PurchasableRepository purchasableRepository;

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course title must not be blank"
            );
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.isBlank() ? null : normalizedDescription;
    }

    private void validateCreateType(PurchasableType type) {
        if (type != PurchasableType.COURSE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Type must be COURSE for course creation"
            );
        }
    }

    private void validateUpdateType(PurchasableType type) {
        if (type != null && type != PurchasableType.COURSE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course type cannot be changed to another type"
            );
        }
    }

    private Course getCourseEntityById(UUID id) {
        return courseRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found with id: " + id
                ));
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with id: " + id
                ));
    }

    public CourseResponse create(CreateCourseRequest request) {
        validateCreateType(request.type());

        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        if (purchasableRepository.existsByTitleIgnoreCase(normalizedTitle)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Course with this title already exists"
            );
        }

        User createdBy = getUserById(request.createdById());
        Category category = getCategoryById(request.categoryId());

        Purchasable purchasable = new Purchasable();
        purchasable.setTitle(normalizedTitle);
        purchasable.setDescription(normalizedDescription);
        purchasable.setType(PurchasableType.COURSE);
        purchasable.setCategory(category);
        purchasable.setCreatedBy(createdBy);

        Purchasable savedPurchasable = purchasableRepository.save(purchasable);

        Course course = new Course();
        course.setPurchasable(savedPurchasable);
        course.setStartDate(request.startDate());
        course.setDurationWeeks(request.durationWeeks());

        Course savedCourse = courseRepository.saveAndFlush(course);

        return courseMapper.toResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(UUID id) {
        Course course = getCourseEntityById(id);
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAll() {
        List<Course> courses = courseRepository.findAllDetailed();
        return courseMapper.toResponseList(courses);
    }

    public CourseResponse update(UUID id, UpdateCourseRequest request) {
        Course course = getCourseEntityById(id);
        Purchasable purchasable = course.getPurchasable();

        validateUpdateType(request.type());

        if (request.title() != null) {
            String normalizedTitle = normalizeTitle(request.title());

            if (purchasableRepository.existsByTitleIgnoreCaseAndIdNot(normalizedTitle, purchasable.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Course with this title already exists"
                );
            }

            purchasable.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            purchasable.setDescription(normalizeDescription(request.description()));
        }

        if (request.categoryId() != null) {
            Category category = getCategoryById(request.categoryId());
            purchasable.setCategory(category);
        }

        if (request.startDate() != null) {
            course.setStartDate(request.startDate());
        }

        if (request.durationWeeks() != null) {
            course.setDurationWeeks(request.durationWeeks());
        }

        Course updatedCourse = courseRepository.saveAndFlush(course);

        return courseMapper.toResponse(updatedCourse);
    }

    public void delete(UUID id) {
        Course course = getCourseEntityById(id);
        Purchasable purchasable = course.getPurchasable();

        courseRepository.delete(course);
        purchasableRepository.delete(purchasable);
    }
}