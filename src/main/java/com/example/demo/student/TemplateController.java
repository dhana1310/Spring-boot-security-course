package com.example.demo.student;

import com.example.demo.model.Student;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

// used for form-based login (MVC - pattern)
@Controller
public class TemplateController {

    private static final List<Student> STUDENTS = Arrays.asList(
      new Student(1, "James Bond"),
      new Student(2, "Maria Jones"),
      new Student(3, "Anna Smith")
    );

    @GetMapping(path = "/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping(path = "/accessForbidden")  // directed to this URL in case of access forbidden for browser clients
    public String getAccessErrorPage() {
        return "accessForbidden";
    }

    @GetMapping(path = "/courses")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMIN_TRAINER')") //method level authorization using spring security
    public String getCoursesPage() {
        return "courses";
    }

    @GetMapping(path = "/studentsHomePage")
    @PreAuthorize("hasAnyAuthority('STUDENT_WRITE', 'STUDENT_READ', 'COURSE_READ', 'COURSE_WRITE')")
    public String getStudentsHomePage() {
        return "studentsHomePage";
    }
}
