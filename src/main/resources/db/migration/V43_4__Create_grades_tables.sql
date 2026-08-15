CREATE TABLE exams (
                       id UUID PRIMARY KEY,
                       ref VARCHAR(50) NOT NULL,
                       course_offering_id UUID NOT NULL,
                       coefficient NUMERIC(10, 4) NOT NULL,

                       CONSTRAINT fk_exams_course_offering
                           FOREIGN KEY (course_offering_id)
                               REFERENCES course_offerings(id),

                       CONSTRAINT chk_exams_coefficient
                           CHECK (coefficient > 0)
);

CREATE UNIQUE INDEX uk_exams_offering_ref_lower
    ON exams (course_offering_id, LOWER(ref));


CREATE TABLE grades (
                        id UUID PRIMARY KEY,
                        exam_id UUID NOT NULL,
                        student_course_enrollment_id UUID NOT NULL,
                        score NUMERIC(5, 2),
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        CONSTRAINT fk_grades_exam
                            FOREIGN KEY (exam_id)
                                REFERENCES exams(id),

                        CONSTRAINT fk_grades_student_course_enrollment
                            FOREIGN KEY (student_course_enrollment_id)
                                REFERENCES student_course_enrollments(id),

                        CONSTRAINT chk_grades_score
                            CHECK (score IS NULL OR (score >= 0 AND score <= 20)),

                        CONSTRAINT uk_grades_exam_student_enrollment
                            UNIQUE (exam_id, student_course_enrollment_id)
);


CREATE TABLE grade_histories (
                                 id UUID PRIMARY KEY,
                                 grade_id UUID NOT NULL,
                                 old_score NUMERIC(5, 2) NOT NULL,
                                 new_score NUMERIC(5, 2) NOT NULL,
                                 reason TEXT NOT NULL,
                                 changed_by UUID NOT NULL,
                                 changed_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                 CONSTRAINT fk_grade_histories_grade
                                     FOREIGN KEY (grade_id)
                                         REFERENCES grades(id),

                                 CONSTRAINT fk_grade_histories_changed_by
                                     FOREIGN KEY (changed_by)
                                         REFERENCES users(id),

                                 CONSTRAINT chk_grade_histories_old_score
                                     CHECK (old_score >= 0 AND old_score <= 20),

                                 CONSTRAINT chk_grade_histories_new_score
                                     CHECK (new_score >= 0 AND new_score <= 20),

                                 CONSTRAINT chk_grade_histories_reason
                                     CHECK (LENGTH(TRIM(reason)) > 0)
);