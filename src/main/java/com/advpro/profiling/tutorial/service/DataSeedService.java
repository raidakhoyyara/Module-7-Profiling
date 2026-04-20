package com.advpro.profiling.tutorial.service;

import com.advpro.profiling.tutorial.model.Course;
import com.advpro.profiling.tutorial.model.Student;
import com.advpro.profiling.tutorial.model.StudentCourse;
import com.advpro.profiling.tutorial.repository.CourseRepository;
import com.advpro.profiling.tutorial.repository.StudentCourseRepository;
import com.advpro.profiling.tutorial.repository.StudentRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author muhammad.khadafi
 */
@Service
public class DataSeedService {

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private StudentCourseRepository studentCourseRepository;

  private static final int NUMBER_OF_STUDENTS = 20_000;
  private static final int NUMBER_OF_COURSES = 10;

  public void seedStudent() {
    Faker faker = new Faker(new Locale("in-ID"));
    List<Student> students = IntStream.range(0, NUMBER_OF_STUDENTS)
        .mapToObj(i -> {
          Student student = new Student();
          student.setStudentCode(faker.code().ean8());
          student.setName(faker.name().fullName());
          student.setFaculty(faker.educator().course());
          student.setGpa(faker.number().randomDouble(2, 2, 4));
          return student;
        })
        .collect(Collectors.toList());
    studentRepository.saveAll(students);
  }

  public void seedCourse() {
    Faker faker = new Faker(new Locale("in-ID"));
    List<Course> courses = IntStream.range(0, NUMBER_OF_COURSES)
        .mapToObj(i -> {
          Course course = new Course();
          course.setCourseCode(faker.code().ean8());
          course.setName(faker.book().title());
          course.setDescription(faker.lorem().sentence());
          return course;
        })
        .collect(Collectors.toList());
    courseRepository.saveAll(courses);
  }

  public void seedStudentCourses() {
    List<Student> students = studentRepository.findAll();
    List<Course> courses = courseRepository.findAll();

    // For each student, randomly select 2 distinct courses and create StudentCourse entries.
    for (Student student : students) {
      // Using Random.ints to select indices; note: may need to adjust if there are not enough courses.
      List<Course> selectedCourses = new Random().ints(0, courses.size())
          .distinct()
          .limit(2)
          .mapToObj(courses::get)
          .collect(Collectors.toList());

      List<StudentCourse> studentCourses = selectedCourses.stream()
          .map(course -> new StudentCourse(student, course))
          .collect(Collectors.toList());

      studentCourseRepository.saveAll(studentCourses);
    }
  }
}