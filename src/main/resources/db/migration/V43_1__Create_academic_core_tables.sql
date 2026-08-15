CREATE TABLE promotions (
                            id UUID PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            start_year INTEGER NOT NULL,
                            end_year INTEGER NOT NULL,

                            CONSTRAINT chk_promotions_years
                                CHECK (end_year >= start_year)
);

CREATE UNIQUE INDEX uk_promotions_name_lower
    ON promotions (LOWER(name));


CREATE TABLE academic_years (
                                id UUID PRIMARY KEY,
                                label VARCHAR(20) NOT NULL,
                                start_date DATE NOT NULL,
                                end_date DATE NOT NULL,

                                CONSTRAINT chk_academic_years_dates
                                    CHECK (end_date > start_date)
);

CREATE UNIQUE INDEX uk_academic_years_label_lower
    ON academic_years (LOWER(label));


CREATE TABLE groups (
                        id UUID PRIMARY KEY,
                        ref VARCHAR(20) NOT NULL
);

CREATE UNIQUE INDEX uk_groups_ref_lower
    ON groups (LOWER(ref));