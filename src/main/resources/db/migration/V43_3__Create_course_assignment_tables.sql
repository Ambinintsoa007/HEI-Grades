CREATE TABLE courses (
                         id UUID PRIMARY KEY,
                         ref VARCHAR(30) NOT NULL,
                         title VARCHAR(255) NOT NULL,
                         credits INTEGER NOT NULL,

                         CONSTRAINT chk_courses_credits
                             CHECK (credits > 0)
);

CREATE UNIQUE INDEX uk_courses_ref_lower
    ON courses (LOWER(ref));


CREATE TABLE course_offerings (
                                  id UUID PRIMARY KEY,
                                  course_id UUID NOT NULL,
                                  academic_year_id UUID NOT NULL,
                                  group_id UUID NOT NULL,

                                  CONSTRAINT fk_course_offerings_course
                                      FOREIGN KEY (course_id) REFERENCES courses(id),

                                  CONSTRAINT fk_course_offerings_academic_year
                                      FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),

                                  CONSTRAINT fk_course_offerings_group
                                      FOREIGN KEY (group_id) REFERENCES groups(id),

                                  CONSTRAINT uk_course_offerings
                                      UNIQUE (course_id, academic_year_id, group_id)
);


CREATE TABLE course_offering_teachers (
                                          id UUID PRIMARY KEY,
                                          course_offering_id UUID NOT NULL,
                                          teacher_id UUID NOT NULL,

                                          CONSTRAINT fk_course_offering_teachers_offering
                                              FOREIGN KEY (course_offering_id) REFERENCES course_offerings(id),

                                          CONSTRAINT fk_course_offering_teachers_teacher
                                              FOREIGN KEY (teacher_id) REFERENCES users(id),

                                          CONSTRAINT uk_course_offering_teachers
                                              UNIQUE (course_offering_id, teacher_id)
);


CREATE TABLE student_course_enrollments (
                                            id UUID PRIMARY KEY,
                                            student_id UUID NOT NULL,
                                            course_id UUID NOT NULL,
                                            academic_year_id UUID NOT NULL,
                                            enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                            CONSTRAINT fk_student_course_enrollments_student
                                                FOREIGN KEY (student_id) REFERENCES users(id),

                                            CONSTRAINT fk_student_course_enrollments_course
                                                FOREIGN KEY (course_id) REFERENCES courses(id),

                                            CONSTRAINT fk_student_course_enrollments_academic_year
                                                FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),

                                            CONSTRAINT uk_student_course_enrollments
                                                UNIQUE (student_id, course_id, academic_year_id)
);