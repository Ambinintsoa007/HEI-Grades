CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       std VARCHAR(30),
                       promotion_id UUID,

                       CONSTRAINT fk_users_promotion
                           FOREIGN KEY (promotion_id)
                               REFERENCES promotions(id),

                       CONSTRAINT chk_users_role
                           CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),

                       CONSTRAINT chk_users_status
                           CHECK (status IN ('ACTIVE', 'DISABLED')),

                       CONSTRAINT chk_student_academic_info
                           CHECK (
                               role <> 'STUDENT'
                                   OR (std IS NOT NULL AND promotion_id IS NOT NULL)
                               )
);

CREATE UNIQUE INDEX uk_users_email_lower
    ON users (LOWER(email));

CREATE UNIQUE INDEX uk_users_std_lower
    ON users (LOWER(std))
    WHERE std IS NOT NULL;


CREATE TABLE student_group_histories (
                                         id UUID PRIMARY KEY,
                                         student_id UUID NOT NULL,
                                         academic_year_id UUID NOT NULL,
                                         group_id UUID NOT NULL,
                                         pathway VARCHAR(10),
                                         started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         ended_at TIMESTAMP WITH TIME ZONE,

                                         CONSTRAINT fk_student_group_histories_student
                                             FOREIGN KEY (student_id)
                                                 REFERENCES users(id),

                                         CONSTRAINT fk_student_group_histories_academic_year
                                             FOREIGN KEY (academic_year_id)
                                                 REFERENCES academic_years(id),

                                         CONSTRAINT fk_student_group_histories_group
                                             FOREIGN KEY (group_id)
                                                 REFERENCES groups(id),

                                         CONSTRAINT chk_student_group_histories_pathway
                                             CHECK (pathway IS NULL OR pathway IN ('EL', 'TN')),

                                         CONSTRAINT chk_student_group_histories_period
                                             CHECK (ended_at IS NULL OR ended_at > started_at)
);