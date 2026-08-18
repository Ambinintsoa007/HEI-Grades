CREATE TABLE student_course_enrollment_offerings (
                                                     student_course_enrollment_id UUID NOT NULL,
                                                     course_offering_id UUID NOT NULL,

                                                     CONSTRAINT fk_enrollment_offering_enrollment
                                                         FOREIGN KEY (student_course_enrollment_id)
                                                             REFERENCES student_course_enrollments(id),

                                                     CONSTRAINT fk_enrollment_offering_offering
                                                         FOREIGN KEY (course_offering_id)
                                                             REFERENCES course_offerings(id),

                                                     CONSTRAINT pk_student_course_enrollment_offerings
                                                         PRIMARY KEY (student_course_enrollment_id, course_offering_id)
);