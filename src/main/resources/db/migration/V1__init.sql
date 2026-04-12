CREATE TYPE ROLE AS ENUM (
    'ADMIN',
    'PROFESSOR',
    'STUDENT'
    );

CREATE TYPE TRANSACTIONTYPE AS ENUM (
    'PAYMENT',
    'REFUND'
    );

CREATE TYPE TRANSACTIONSTATUS AS ENUM (
    'CANCELLED',
    'FAILED',
    'PENDING',
    'REJECTED',
    'SUCCESS'
    );

CREATE TYPE PAYMENTMETHOD AS ENUM (
    'CARD',
    'CASH',
    'PAYPAL',
    'STRIPE'
    );

CREATE TYPE REGISTRATIONSTATUS AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'SUSPENDED'
    );

CREATE TYPE PURCHASABLETYPE AS ENUM (
    'COURSE',
    'EXAM'
    );

CREATE TYPE ATTENDANCESTATUS AS ENUM (
    'ATTENDED',
    'ENROLLED',
    'MISSED'
    );

CREATE TYPE SESSIONSTATE AS ENUM (
    'CANCELED',
    'COMPLETED',
    'ONGOING',
    'SCHEDULED'
    );

CREATE TYPE SESSIONTYPE AS ENUM (
    'MEET_LINK',
    'VIDEO_LINK'
    );

CREATE TYPE TASKTYPE AS ENUM (
    'CODE',
    'ESSAY',
    'FILE_UPLOAD',
    'QUIZ'
    );

CREATE TYPE HOMEWORKSTATUS AS ENUM (
    'FAILED',
    'PASSED',
    'PENDING_REVIEW'
    );

CREATE TYPE EXAMSTATUS AS ENUM (
    'FAILED',
    'PASSED',
    'PENDING_REVIEW'
    );

CREATE TABLE categories
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    title       VARCHAR(50)                 NOT NULL,
    description VARCHAR(255),
    CONSTRAINT categories_pkey PRIMARY KEY (id)
);

CREATE TABLE certificates
(
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at       TIMESTAMP WITHOUT TIME ZONE,
    expiry_date      TIMESTAMP WITHOUT TIME ZONE,
    issue_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id               UUID                        NOT NULL,
    purchasable_id   UUID                        NOT NULL,
    user_id          UUID                        NOT NULL,
    certificate_code VARCHAR(255)                NOT NULL,
    pdf_url          VARCHAR(255),
    metadata         JSONB                       NOT NULL,
    CONSTRAINT certificates_pkey PRIMARY KEY (id)
);

CREATE TABLE courses
(
    duration_weeks INTEGER                     NOT NULL,
    start_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    id             UUID                        NOT NULL,
    purchasable_id UUID                        NOT NULL,
    CONSTRAINT courses_pkey PRIMARY KEY (id)
);

CREATE TABLE exam_sections
(
    duration    INTERVAL,
    order_index INTEGER                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    exam_id     UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    description VARCHAR(255),
    title       VARCHAR(255)                NOT NULL,
    CONSTRAINT exam_sections_pkey PRIMARY KEY (id)
);

CREATE TABLE exam_submissions
(
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id             UUID                        NOT NULL,
    task_id        UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    note           VARCHAR(255),
    answer_payload JSONB                       NOT NULL,
    status         EXAMSTATUS                  NOT NULL,
    CONSTRAINT exam_submissions_pkey PRIMARY KEY (id)
);

CREATE TABLE exam_tasks
(
    duration        INTERVAL,
    order_index     INTEGER                     NOT NULL,
    point           INTEGER                     NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by      UUID                        NOT NULL,
    id              UUID                        NOT NULL,
    section_id      UUID                        NOT NULL,
    type            TASKTYPE                    NOT NULL,
    content_payload JSONB                       NOT NULL,
    CONSTRAINT exam_tasks_pkey PRIMARY KEY (id)
);

CREATE TABLE exams
(
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id             UUID                        NOT NULL,
    purchasable_id UUID                        NOT NULL,
    CONSTRAINT exams_pkey PRIMARY KEY (id)
);

CREATE TABLE homework_submissions
(
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id             UUID                        NOT NULL,
    task_id        UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    note           VARCHAR(255),
    answer_payload JSONB                       NOT NULL,
    status         HOMEWORKSTATUS              NOT NULL,
    CONSTRAINT homework_submissions_pkey PRIMARY KEY (id)
);

CREATE TABLE homework_tasks
(
    order_index     INTEGER                     NOT NULL,
    point           INTEGER                     NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by      UUID                        NOT NULL,
    homework_id     UUID                        NOT NULL,
    id              UUID                        NOT NULL,
    payload_content JSONB                       NOT NULL,
    type            TASKTYPE                    NOT NULL,
    CONSTRAINT homework_tasks_pkey PRIMARY KEY (id)
);

CREATE TABLE homework_translations
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    homework_id UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    code        VARCHAR(2)                  NOT NULL,
    description VARCHAR(255),
    title       VARCHAR(255)                NOT NULL,
    CONSTRAINT homework_translations_pkey PRIMARY KEY (id)
);

CREATE TABLE homeworks
(
    is_published BOOLEAN                     NOT NULL,
    order_index  INTEGER                     NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at   TIMESTAMP WITHOUT TIME ZONE,
    due_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by   UUID                        NOT NULL,
    id           UUID                        NOT NULL,
    lesson_id    UUID                        NOT NULL,
    title        VARCHAR(50)                 NOT NULL,
    description  VARCHAR(255),
    CONSTRAINT homeworks_pkey PRIMARY KEY (id)
);

CREATE TABLE languages
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by UUID                        NOT NULL,
    name       VARCHAR(50)                 NOT NULL,
    code       VARCHAR(2)                  NOT NULL,
    CONSTRAINT languages_pkey PRIMARY KEY (code)
);

CREATE TABLE lesson_attendances
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id         UUID                        NOT NULL,
    lesson_id  UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    note       VARCHAR(255),
    status     ATTENDANCESTATUS            NOT NULL,
    CONSTRAINT lesson_attendances_pkey PRIMARY KEY (id)
);

CREATE TABLE lesson_translations
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    lesson_id   UUID                        NOT NULL,
    code        VARCHAR(2)                  NOT NULL,
    description VARCHAR(255),
    title       VARCHAR(255)                NOT NULL,
    CONSTRAINT lesson_translations_pkey PRIMARY KEY (id)
);

CREATE TABLE lessons
(
    duration    INTERVAL                    NOT NULL,
    order_index INTEGER                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    starts_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    course_id   UUID                        NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    title       VARCHAR(50)                 NOT NULL,
    description VARCHAR(255),
    value_url   VARCHAR(255)                NOT NULL,
    type        SESSIONTYPE                 NOT NULL,
    state       SESSIONSTATE                NOT NULL,
    CONSTRAINT lessons_pkey PRIMARY KEY (id)
);

CREATE TABLE product_purchasables
(
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id             UUID                        NOT NULL,
    product_id     UUID                        NOT NULL,
    purchasable_id UUID                        NOT NULL,
    CONSTRAINT product_purchasables_pkey PRIMARY KEY (id)
);

CREATE TABLE product_registrations
(
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at     TIMESTAMP WITHOUT TIME ZONE,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id             UUID                        NOT NULL,
    product_id     UUID                        NOT NULL,
    transaction_id UUID,
    user_id        UUID                        NOT NULL,
    status         REGISTRATIONSTATUS          NOT NULL,
    CONSTRAINT product_registrations_pkey PRIMARY KEY (id)
);

CREATE TABLE product_translations
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    product_id  UUID                        NOT NULL,
    code        VARCHAR(2)                  NOT NULL,
    description VARCHAR(255),
    title       VARCHAR(255)                NOT NULL,
    CONSTRAINT product_translations_pkey PRIMARY KEY (id)
);

CREATE TABLE products
(
    is_private  BOOLEAN,
    price       numeric(10, 2)              NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    title       VARCHAR(50)                 NOT NULL,
    description VARCHAR(255),
    type        PURCHASABLETYPE             NOT NULL,
    CONSTRAINT products_pkey PRIMARY KEY (id)
);

CREATE TABLE professors
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    course_id  UUID                        NOT NULL,
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    CONSTRAINT professors_pkey PRIMARY KEY (id)
);

CREATE TABLE purchasable_translations
(
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by     UUID                        NOT NULL,
    id             UUID                        NOT NULL,
    purchasable_id UUID                        NOT NULL,
    code           VARCHAR(2)                  NOT NULL,
    description    VARCHAR(255),
    title          VARCHAR(255)                NOT NULL,
    CONSTRAINT purchasable_translations_pkey PRIMARY KEY (id)
);

CREATE TABLE purchasables
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    category_id UUID                        NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    title       VARCHAR(50)                 NOT NULL,
    description VARCHAR(255),
    type        PURCHASABLETYPE             NOT NULL,
    CONSTRAINT purchasables_pkey PRIMARY KEY (id)
);

CREATE TABLE remember_me_tokens
(
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id           UUID                        NOT NULL,
    user_id      UUID                        NOT NULL,
    selector     VARCHAR(64)                 NOT NULL,
    token_hash   VARCHAR(64)                 NOT NULL,
    CONSTRAINT remember_me_tokens_pkey PRIMARY KEY (id)
);

CREATE TABLE section_translations
(
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  UUID                        NOT NULL,
    id          UUID                        NOT NULL,
    section_id  UUID                        NOT NULL,
    code        VARCHAR(2)                  NOT NULL,
    description VARCHAR(255),
    title       VARCHAR(255)                NOT NULL,
    CONSTRAINT section_translations_pkey PRIMARY KEY (id)
);

CREATE TABLE transactions
(
    amount           numeric(10, 2)              NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id               UUID                        NOT NULL,
    product_id       UUID                        NOT NULL,
    user_id          UUID                        NOT NULL,
    currency         VARCHAR(3) DEFAULT 'USD'    NOT NULL,
    payment_method   PAYMENTMETHOD               NOT NULL,
    status           TRANSACTIONSTATUS           NOT NULL,
    transaction_type TRANSACTIONTYPE             NOT NULL,
    CONSTRAINT transactions_pkey PRIMARY KEY (id)
);

CREATE TABLE user_processes
(
    current_step      INTEGER                     NOT NULL,
    score_accumulated numeric(10, 2),
    total_steps       INTEGER                     NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_accessed_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id                UUID                        NOT NULL,
    purchasable_id    UUID                        NOT NULL,
    user_id           UUID                        NOT NULL,
    CONSTRAINT user_processes_pkey PRIMARY KEY (id)
);

CREATE TABLE users
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id         UUID                        NOT NULL,
    first_name VARCHAR(50)                 NOT NULL,
    last_name  VARCHAR(50)                 NOT NULL,
    avatar_url VARCHAR(255),
    bio        VARCHAR(255),
    email      VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    role       ROLE                        NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE courses
    ADD CONSTRAINT courses_purchasable_id_key UNIQUE (purchasable_id);

ALTER TABLE exams
    ADD CONSTRAINT exams_purchasable_id_key UNIQUE (purchasable_id);

ALTER TABLE remember_me_tokens
    ADD CONSTRAINT remember_me_tokens_selector_key UNIQUE (selector);

ALTER TABLE remember_me_tokens
    ADD CONSTRAINT remember_me_token_user_key UNIQUE (user_id);

ALTER TABLE product_purchasables
    ADD CONSTRAINT uk_product_purchasable UNIQUE (product_id, purchasable_id);

ALTER TABLE user_processes
    ADD CONSTRAINT uk_user_process_user_purchsable UNIQUE (user_id, purchasable_id);

CREATE UNIQUE INDEX uk_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_exam_section_order_index_exam_active
    ON exam_sections (exam_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_exam_task_order_index_section_active
    ON exam_tasks (section_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_homework_order_index_lesson_active
    ON homeworks (lesson_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_homework_task_order_index_homework_active
    ON homework_tasks (homework_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_lesson_order_index_course_active
    ON lessons (course_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_product_registration_user_product_active
    ON product_registrations (user_id, product_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_professor_user_course_active
    ON professors (user_id, course_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_product_translation_code ON product_translations (code);

CREATE INDEX idx_purchasable_translation_code ON purchasable_translations (code);

CREATE INDEX idx_remember_me_token_expires_at ON remember_me_tokens (expires_at);

CREATE INDEX idx_section_translation_code ON section_translations (code);

ALTER TABLE lessons
    ADD CONSTRAINT fk17ucc7gjfjddsyi0gvstkqeat FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE NO ACTION;

ALTER TABLE certificates
    ADD CONSTRAINT fk2dq3nyt5ohrjjgon1gov34pe FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

CREATE INDEX idx_certificate_purchasable_id ON certificates (purchasable_id);

ALTER TABLE lesson_translations
    ADD CONSTRAINT fk2v8n4bc3hwsmbiys5tf86f7d0 FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE NO ACTION;

CREATE INDEX uk_lesson_translation_lesson_id ON lesson_translations (lesson_id);

ALTER TABLE exam_submissions
    ADD CONSTRAINT fk34h9a5v9c8o3m7v0r9bhqvlsv FOREIGN KEY (task_id) REFERENCES exam_tasks (id) ON DELETE NO ACTION;

CREATE INDEX idx_exam_submissions_task_id ON exam_submissions (task_id);

ALTER TABLE purchasables
    ADD CONSTRAINT fk45afgnj5p4pmmd97txhc8e11k FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE courses
    ADD CONSTRAINT fk47hchcresx01fk14rg056eta6 FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

ALTER TABLE lessons
    ADD CONSTRAINT fk5dunsor7s6dt8g0hvt28r422q FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE categories
    ADD CONSTRAINT fk5yfru0au6kpyqs4tonky5vfne FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE professors
    ADD CONSTRAINT fk6vp5482314kllcbx5dss173ph FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE NO ACTION;

ALTER TABLE exam_sections
    ADD CONSTRAINT fk6xh5uoyt87amqhmx40wgdjmic FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE homework_translations
    ADD CONSTRAINT fk7trw9kci7njdca9n7jd08v587 FOREIGN KEY (homework_id) REFERENCES homeworks (id) ON DELETE NO ACTION;

CREATE INDEX idx_homework_translation_homework_id ON homework_translations (homework_id);

ALTER TABLE exam_tasks
    ADD CONSTRAINT fk88k96lnyagjatu3hf52bfr497 FOREIGN KEY (section_id) REFERENCES exam_sections (id) ON DELETE NO ACTION;

ALTER TABLE exam_submissions
    ADD CONSTRAINT fk8uff6g6nes0c0ndr1s8lpop5f FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_exam_submissions_user_id ON exam_submissions (user_id);

ALTER TABLE exam_sections
    ADD CONSTRAINT fk9f071ketmsh5txvih16uifs15 FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE NO ACTION;

CREATE INDEX idx_exam_section_exam_id ON exam_sections (exam_id);

ALTER TABLE product_purchasables
    ADD CONSTRAINT fk9xs89bejewgq1floqsns0a6br FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION;

ALTER TABLE transactions
    ADD CONSTRAINT fkcdpkn7bkq15bjvlw9mo46l9ft FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION;

CREATE INDEX idx_transaction_product_id ON transactions (product_id);

ALTER TABLE lesson_translations
    ADD CONSTRAINT fkci2dki2eyk5esg2p9emch5lw2 FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE certificates
    ADD CONSTRAINT fkd3f6enpb3p3xovee9klklf05r FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_certificate_user_id ON certificates (user_id);

ALTER TABLE homework_tasks
    ADD CONSTRAINT fkeec9wygl4yqm00fgs9aq9e6p1 FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE purchasable_translations
    ADD CONSTRAINT fkelcnl32ekugfpk6tdild5vt6q FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

CREATE INDEX idx_purchasable_translation_purchasable_id ON purchasable_translations (purchasable_id);

ALTER TABLE homework_tasks
    ADD CONSTRAINT fkfufl2t8wie21geq7dnsbrqloo FOREIGN KEY (homework_id) REFERENCES homeworks (id) ON DELETE NO ACTION;

ALTER TABLE user_processes
    ADD CONSTRAINT fkfw71dtfc0sg226earbl3ihf9m FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

ALTER TABLE product_purchasables
    ADD CONSTRAINT fkgck009mv241i8ank7c1tacdeg FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

ALTER TABLE languages
    ADD CONSTRAINT fkhlgtg1bdn7aptabt0eovlct61 FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE lesson_attendances
    ADD CONSTRAINT fkhlvrdq9v6k411spm1bmqol734 FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE NO ACTION;

CREATE INDEX idx_lesson_attendance_lesson_id ON lesson_attendances (lesson_id);

ALTER TABLE product_registrations
    ADD CONSTRAINT fkhpquilnfb0qu6qi9bq1xkwqil FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE lesson_attendances
    ADD CONSTRAINT fkjs6un555qqw1a2muowsqumc4g FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_lesson_attendance_user_id ON lesson_attendances (user_id);

ALTER TABLE exam_tasks
    ADD CONSTRAINT fkk2d5ipolb3kpm4v24p3a36c63 FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE product_registrations
    ADD CONSTRAINT fkkfn7r0xif8tbokrghgnbitbqr FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION;

ALTER TABLE homeworks
    ADD CONSTRAINT fkkt5ecyq4ovj1qrnvh3pow2aoc FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE NO ACTION;

ALTER TABLE products
    ADD CONSTRAINT fkl0lce8i162ldn9n01t2a6lcix FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE purchasables
    ADD CONSTRAINT fklaitcxhllurft1bm5nivmqjyw FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE NO ACTION;

CREATE INDEX idx_purchasable_category_id ON purchasables (category_id);

ALTER TABLE exams
    ADD CONSTRAINT fklcbi4316wt46k033xmrwhn4gs FOREIGN KEY (purchasable_id) REFERENCES purchasables (id) ON DELETE NO ACTION;

ALTER TABLE professors
    ADD CONSTRAINT fklq1bc4wecor3b2lr0whjiimy8 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE product_translations
    ADD CONSTRAINT fkn8aifokvlqtmaoriv3st4tk7x FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE remember_me_tokens
    ADD CONSTRAINT fknq7xt95sjsy8spceb2x7dhilg FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_remember_me_token_user_id ON remember_me_tokens (user_id);

ALTER TABLE homework_translations
    ADD CONSTRAINT fkojl0s3lusk7oirnmgxmmc8xyw FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE product_translations
    ADD CONSTRAINT fkom5nwwno2wotmalniq34w627y FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE NO ACTION;

CREATE INDEX idx_product_translation_product_id ON product_translations (product_id);

ALTER TABLE homeworks
    ADD CONSTRAINT fkpxvah0n3qfiqai9wsijwmotub FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE purchasable_translations
    ADD CONSTRAINT fkq1cel3133bt8kjvsjpicbgjxm FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE section_translations
    ADD CONSTRAINT fkq4i3as3p26t2oiyfqebmm7tj FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE user_processes
    ADD CONSTRAINT fkqog4sbq5o1pqyf56wkaohl8ym FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE transactions
    ADD CONSTRAINT fkqwv7rmvc8va8rep7piikrojds FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_transaction_user_id ON transactions (user_id);

ALTER TABLE section_translations
    ADD CONSTRAINT fkrf3q252um26shvr5jlkn83g9c FOREIGN KEY (section_id) REFERENCES exam_sections (id) ON DELETE NO ACTION;

CREATE INDEX idx_section_translation_section_id ON section_translations (section_id);

ALTER TABLE homework_submissions
    ADD CONSTRAINT fkrsi0ojxyrlyga9t0rfr9f3d0 FOREIGN KEY (task_id) REFERENCES homework_tasks (id) ON DELETE NO ACTION;

CREATE INDEX idx_homework_submission_task_id ON homework_submissions (task_id);

ALTER TABLE product_registrations
    ADD CONSTRAINT fkt127ikiah8xmo1tva586smy98 FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE NO ACTION;

CREATE INDEX idx_product_registration_transaction_id ON product_registrations (transaction_id);

ALTER TABLE homework_submissions
    ADD CONSTRAINT fkt6djgatws7xxgcrrptuogn6ka FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX idx_homework_submission_user_id ON homework_submissions (user_id);