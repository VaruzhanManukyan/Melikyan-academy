CREATE TYPE ROLE AS ENUM (
    'ADMIN',
    'PROFESSOR',
    'STUDENT'
    );

CREATE TYPE TRANSACTION_TYPE AS ENUM (
    'PAYMENT',
    'REFUND'
    );

CREATE TYPE TRANSACTION_STATUS AS ENUM (
    'CANCELLED',
    'FAILED',
    'PENDING',
    'REJECTED',
    'SUCCESS'
    );

CREATE TYPE PAYMENT_METHOD AS ENUM (
    'CARD',
    'CASH',
    'PAYPAL',
    'STRIPE'
    );

CREATE TYPE REGISTRATION_STATUS AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'SUSPENDED'
    );

CREATE TYPE CONTENT_ITEM_TYPE AS ENUM (
    'COURSE',
    'EXAM'
    );

CREATE TYPE ATTENDANCE_STATUS AS ENUM (
    'ATTENDED',
    'ENROLLED',
    'MISSED'
    );

CREATE TYPE LESSON_STATE AS ENUM (
    'CANCELED',
    'COMPLETED',
    'ONGOING',
    'SCHEDULED'
    );

CREATE TYPE LESSON_TYPE AS ENUM (
    'MEET_LINK',
    'VIDEO_LINK'
    );

CREATE TYPE TASK_TYPE AS ENUM (
    'CODE',
    'ESSAY',
    'FILE_UPLOAD',
    'QUIZ'
    );

CREATE TYPE HOMEWORK_STATUS AS ENUM (
    'FAILED',
    'PASSED',
    'PENDING_REVIEW'
    );

CREATE TYPE EXAM_STATUS AS ENUM (
    'FAILED',
    'PASSED',
    'PENDING_REVIEW'
    );

CREATE TYPE PRODUCT_TYPE AS ENUM (
    'SINGLE',
    'PACKAGE'
    );

CREATE TABLE users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    role       ROLE         NOT NULL,
    bio        VARCHAR(255),
    avatar_url VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT chk_users_email_not_blank
        CHECK (btrim(email) <> ''),
    CONSTRAINT chk_users_password_not_blank
        CHECK (btrim(password) <> ''),
    CONSTRAINT chk_users_first_name_not_blank
        CHECK (btrim(first_name) <> ''),
    CONSTRAINT chk_users_last_name_not_blank
        CHECK (btrim(last_name) <> '')
);

CREATE TABLE categories
(
    id          UUID        NOT NULL,
    title       VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_by  UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT chk_categories_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_categories_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE products
(
    id          UUID           NOT NULL,
    title       VARCHAR(50),
    description VARCHAR(255),
    type        PRODUCT_TYPE   NOT NULL DEFAULT 'SINGLE',
    price       NUMERIC(10, 2) NOT NULL,
    is_private  BOOLEAN        NOT NULL DEFAULT FALSE,
    category_id UUID           NOT NULL,
    created_by  UUID           NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT chk_products_title_valid
        CHECK (
            (type = 'PACKAGE' AND title IS NOT NULL AND btrim(title) <> '')
                OR
            (type = 'SINGLE' AND (title IS NULL OR btrim(title) <> ''))
            ),
    CONSTRAINT chk_products_price_positive
        CHECK (price > 0),
    CONSTRAINT fk_products_category_id__categories
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE languages
(
    code       VARCHAR(2)  NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_by UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT pk_languages PRIMARY KEY (code),
    CONSTRAINT chk_languages_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_languages_name_not_blank
        CHECK (btrim(name) <> ''),
    CONSTRAINT fk_languages_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE remember_me_tokens
(
    id           UUID         NOT NULL,
    selector     VARCHAR(64)  NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    last_used_at TIMESTAMPTZ  NOT NULL,
    user_id      UUID         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_remember_me_tokens PRIMARY KEY (id),
    CONSTRAINT uq_remember_me_tokens_selector UNIQUE (selector),
    CONSTRAINT chk_remember_me_tokens_selector_not_blank
        CHECK (btrim(selector) <> ''),
    CONSTRAINT chk_remember_me_tokens_token_hash_not_blank
        CHECK (btrim(token_hash) <> ''),
    CONSTRAINT fk_remember_me_tokens_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE content_items
(
    id          UUID              NOT NULL,
    type        CONTENT_ITEM_TYPE NOT NULL,
    title       VARCHAR(50)       NOT NULL,
    description VARCHAR(255),
    total_steps INTEGER           NOT NULL,
    created_by  UUID              NOT NULL,
    created_at  TIMESTAMPTZ       NOT NULL,
    updated_at  TIMESTAMPTZ       NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_content_items PRIMARY KEY (id),
    CONSTRAINT chk_content_item_total_steps_positive
        CHECK (total_steps >= 0),
    CONSTRAINT chk_content_items_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_content_items_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE courses
(
    id              UUID        NOT NULL,
    duration_weeks  INTEGER     NOT NULL,
    start_date      TIMESTAMPTZ NOT NULL,
    content_item_id UUID        NOT NULL,
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT uq_courses_content_item_id UNIQUE (content_item_id),
    CONSTRAINT chk_courses_duration_weeks_positive
        CHECK (duration_weeks > 0),
    CONSTRAINT fk_courses_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id) ON DELETE CASCADE
);

CREATE TABLE exams
(
    id              UUID        NOT NULL,
    content_item_id UUID        NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_exams PRIMARY KEY (id),
    CONSTRAINT uq_exams_content_item_id UNIQUE (content_item_id),
    CONSTRAINT fk_exams_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id) ON DELETE CASCADE
);

CREATE TABLE professors
(
    id         UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    course_id  UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT pk_professors PRIMARY KEY (id),
    CONSTRAINT fk_professors_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_professors_course_id__courses
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

CREATE TABLE lessons
(
    id          UUID         NOT NULL,
    order_index INTEGER      NOT NULL,
    type        LESSON_TYPE  NOT NULL,
    state       LESSON_STATE NOT NULL DEFAULT 'SCHEDULED',
    title       VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    value_url   VARCHAR(255) NOT NULL,
    duration    INTERVAL     NOT NULL,
    starts_at   TIMESTAMPTZ  NOT NULL,
    course_id   UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_lessons PRIMARY KEY (id),
    CONSTRAINT chk_lessons_order_index_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_lessons_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT chk_lessons_value_url_not_blank
        CHECK (btrim(value_url) <> ''),
    CONSTRAINT chk_lessons_duration_positive
        CHECK (duration > INTERVAL '0'),
    CONSTRAINT fk_lessons_course_id__courses
        FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_lessons_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE lesson_attendances
(
    id         UUID              NOT NULL,
    status     ATTENDANCE_STATUS NOT NULL,
    note       VARCHAR(255),
    user_id    UUID              NOT NULL,
    lesson_id  UUID              NOT NULL,
    created_at TIMESTAMPTZ       NOT NULL,
    updated_at TIMESTAMPTZ       NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT pk_lesson_attendances PRIMARY KEY (id),
    CONSTRAINT fk_lesson_attendances_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_lesson_attendances_lesson_id__lessons
        FOREIGN KEY (lesson_id) REFERENCES lessons (id)
);

CREATE TABLE lesson_translations
(
    id          UUID         NOT NULL,
    code        VARCHAR(2)   NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    lesson_id   UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_lesson_translations PRIMARY KEY (id),
    CONSTRAINT uq_lesson_translations_lesson_id_code
        UNIQUE (lesson_id, code),
    CONSTRAINT chk_lesson_translations_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_lesson_translations_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_lesson_translations_lesson_id__lessons
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_translations_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE homeworks
(
    id           UUID        NOT NULL,
    order_index  INTEGER     NOT NULL,
    title        VARCHAR(50) NOT NULL,
    description  VARCHAR(255),
    is_published BOOLEAN     NOT NULL DEFAULT FALSE,
    due_date     TIMESTAMPTZ NOT NULL,
    lesson_id    UUID        NOT NULL,
    created_by   UUID        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    deleted_at   TIMESTAMPTZ,

    CONSTRAINT pk_homeworks PRIMARY KEY (id),
    CONSTRAINT chk_homeworks_order_index_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_homeworks_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_homeworks_lesson_id__lessons
        FOREIGN KEY (lesson_id) REFERENCES lessons (id),
    CONSTRAINT fk_homeworks_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE homework_tasks
(
    id              UUID        NOT NULL,
    order_index     INTEGER     NOT NULL,
    type            TASK_TYPE   NOT NULL,
    point           INTEGER     NOT NULL,
    payload_content JSONB       NOT NULL,
    homework_id     UUID        NOT NULL,
    created_by      UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_homework_tasks PRIMARY KEY (id),
    CONSTRAINT chk_homework_tasks_order_index_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_homework_tasks_point_positive
        CHECK (point > 0),
    CONSTRAINT fk_homework_tasks_homework_id__homeworks
        FOREIGN KEY (homework_id) REFERENCES homeworks (id),
    CONSTRAINT fk_homework_tasks_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE homework_submissions
(
    id             UUID            NOT NULL,
    status         HOMEWORK_STATUS NOT NULL DEFAULT 'PENDING_REVIEW',
    note           VARCHAR(255),
    answer_payload JSONB           NOT NULL,
    task_id        UUID            NOT NULL,
    user_id        UUID            NOT NULL,
    created_at     TIMESTAMPTZ     NOT NULL,
    updated_at     TIMESTAMPTZ     NOT NULL,
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT pk_homework_submissions PRIMARY KEY (id),
    CONSTRAINT fk_homework_submissions_task_id__homework_tasks
        FOREIGN KEY (task_id) REFERENCES homework_tasks (id),
    CONSTRAINT fk_homework_submissions_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE homework_translations
(
    id          UUID         NOT NULL,
    code        VARCHAR(2)   NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    homework_id UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_homework_translations PRIMARY KEY (id),
    CONSTRAINT uq_homework_translations_homework_id_code
        UNIQUE (homework_id, code),
    CONSTRAINT chk_homework_translations_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_homework_translations_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_homework_translations_homework_id__homeworks
        FOREIGN KEY (homework_id) REFERENCES homeworks (id) ON DELETE CASCADE,
    CONSTRAINT fk_homework_translations_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE exam_sections
(
    id          UUID         NOT NULL,
    order_index INTEGER      NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    duration    INTERVAL,
    exam_id     UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_exam_sections PRIMARY KEY (id),
    CONSTRAINT chk_exam_sections_order_index_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_exam_sections_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT chk_exam_sections_duration_positive
        CHECK (duration IS NULL OR duration > INTERVAL '0'),
    CONSTRAINT fk_exam_sections_exam_id__exams
        FOREIGN KEY (exam_id) REFERENCES exams (id),
    CONSTRAINT fk_exam_sections_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE exam_tasks
(
    id              UUID        NOT NULL,
    order_index     INTEGER     NOT NULL,
    type            TASK_TYPE   NOT NULL,
    duration        INTERVAL,
    point           INTEGER     NOT NULL,
    content_payload JSONB       NOT NULL,
    section_id      UUID        NOT NULL,
    created_by      UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_exam_tasks PRIMARY KEY (id),
    CONSTRAINT chk_exam_tasks_order_index_positive
        CHECK (order_index > 0),
    CONSTRAINT chk_exam_tasks_point_positive
        CHECK (point > 0),
    CONSTRAINT chk_exam_tasks_duration_positive
        CHECK (duration IS NULL OR duration > INTERVAL '0'),
    CONSTRAINT fk_exam_tasks_section_id__exam_sections
        FOREIGN KEY (section_id) REFERENCES exam_sections (id),
    CONSTRAINT fk_exam_tasks_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE exam_submissions
(
    id             UUID        NOT NULL,
    status         EXAM_STATUS NOT NULL,
    note           VARCHAR(255),
    answer_payload JSONB       NOT NULL,
    task_id        UUID        NOT NULL,
    user_id        UUID        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT pk_exam_submissions PRIMARY KEY (id),
    CONSTRAINT fk_exam_submissions_task_id__exam_tasks
        FOREIGN KEY (task_id) REFERENCES exam_tasks (id),
    CONSTRAINT fk_exam_submissions_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE section_translations
(
    id          UUID         NOT NULL,
    code        VARCHAR(2)   NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    section_id  UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_section_translations PRIMARY KEY (id),
    CONSTRAINT uq_section_translations_section_id_code
        UNIQUE (section_id, code),
    CONSTRAINT chk_section_translations_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_section_translations_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_section_translations_section_id__exam_sections
        FOREIGN KEY (section_id) REFERENCES exam_sections (id) ON DELETE CASCADE,
    CONSTRAINT fk_section_translations_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE product_content_items
(
    id              UUID        NOT NULL,
    product_id      UUID        NOT NULL,
    content_item_id UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_product_content_items PRIMARY KEY (id),
    CONSTRAINT uq_product_content_items_product_id_content_item_id
        UNIQUE (product_id, content_item_id),
    CONSTRAINT fk_product_content_items_product_id__products
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_content_items_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id) ON DELETE CASCADE
);

CREATE TABLE transactions
(
    id               UUID               NOT NULL,
    status           TRANSACTION_STATUS NOT NULL,
    payment_method   PAYMENT_METHOD     NOT NULL,
    transaction_type TRANSACTION_TYPE   NOT NULL,
    amount           NUMERIC(10, 2)     NOT NULL,
    currency         VARCHAR(3)         NOT NULL DEFAULT 'USD',
    user_id          UUID               NOT NULL,
    product_id       UUID               NOT NULL,
    created_at       TIMESTAMPTZ        NOT NULL,

    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT chk_transactions_amount_positive
        CHECK (amount > 0),
    CONSTRAINT chk_transactions_currency_not_blank
        CHECK (btrim(currency) <> ''),
    CONSTRAINT fk_transactions_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_product_id__products
        FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE product_registrations
(
    id             UUID                NOT NULL,
    status         REGISTRATION_STATUS NOT NULL DEFAULT 'ACTIVE',
    user_id        UUID                NOT NULL,
    product_id     UUID                NOT NULL,
    transaction_id UUID,
    created_at     TIMESTAMPTZ         NOT NULL,
    updated_at     TIMESTAMPTZ         NOT NULL,
    deleted_at     TIMESTAMPTZ,

    CONSTRAINT pk_product_registrations PRIMARY KEY (id),
    CONSTRAINT fk_product_registrations_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_product_registrations_product_id__products
        FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_registrations_transaction_id__transactions
        FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

CREATE TABLE product_translations
(
    id          UUID         NOT NULL,
    code        VARCHAR(2)   NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    product_id  UUID         NOT NULL,
    created_by  UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_product_translations PRIMARY KEY (id),
    CONSTRAINT uq_product_translations_product_id_code
        UNIQUE (product_id, code),
    CONSTRAINT chk_product_translations_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_product_translations_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_product_translations_product_id__products
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_translations_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE content_item_translations
(
    id              UUID         NOT NULL,
    code            VARCHAR(2)   NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    content_item_id UUID         NOT NULL,
    created_by      UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_content_item_translations PRIMARY KEY (id),
    CONSTRAINT uq_content_item_translations_content_item_id_code
        UNIQUE (content_item_id, code),
    CONSTRAINT chk_content_item_translations_code_not_blank
        CHECK (btrim(code) <> ''),
    CONSTRAINT chk_content_item_translations_title_not_blank
        CHECK (btrim(title) <> ''),
    CONSTRAINT fk_content_item_translations_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_content_item_translations_created_by__users
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE certificates
(
    id               UUID         NOT NULL,
    issue_date       TIMESTAMPTZ  NOT NULL,
    expiry_date      TIMESTAMPTZ,
    pdf_url          VARCHAR(255),
    metadata         JSONB        NOT NULL,
    certificate_code VARCHAR(255) NOT NULL,
    user_id          UUID         NOT NULL,
    content_item_id  UUID         NOT NULL,
    issued_by        UUID         NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    deleted_at       TIMESTAMPTZ,

    CONSTRAINT pk_certificates PRIMARY KEY (id),
    CONSTRAINT chk_certificates_certificate_code_not_blank
        CHECK (btrim(certificate_code) <> ''),
    CONSTRAINT chk_certificates_expiry_after_issue
        CHECK (expiry_date IS NULL OR expiry_date > issue_date),
    CONSTRAINT fk_certificates_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_certificates_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id),
    CONSTRAINT fk_certificates_issued_by__users
        FOREIGN KEY (issued_by) REFERENCES users (id)
);

CREATE TABLE user_processes
(
    id                UUID        NOT NULL,
    current_step      INTEGER     NOT NULL,
    last_accessed_at  TIMESTAMPTZ,
    score_accumulated NUMERIC(10, 2),
    user_id           UUID        NOT NULL,
    content_item_id   UUID        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_user_processes PRIMARY KEY (id),
    CONSTRAINT uq_user_processes_user_id_content_item_id
        UNIQUE (user_id, content_item_id),
    CONSTRAINT chk_user_processes_current_step_non_negative
        CHECK (current_step >= 0),
    CONSTRAINT chk_user_processes_score_accumulated_non_negative
        CHECK (score_accumulated IS NULL OR score_accumulated >= 0),
    CONSTRAINT fk_user_processes_user_id__users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_processes_content_item_id__content_items
        FOREIGN KEY (content_item_id) REFERENCES content_items (id)
);


CREATE UNIQUE INDEX uidx_users_email__active
    ON users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_categories_title__active
    ON categories (title)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_content_items_type_title__active
    ON content_items (type, title)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_professors_user_id_course_id__active
    ON professors (user_id, course_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_lessons_course_id_order_index__active
    ON lessons (course_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_lessons_course_id_title__active
    ON lessons (course_id, title)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_homeworks_lesson_id_order_index__active
    ON homeworks (lesson_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_homeworks_lesson_id_title__active
    ON homeworks (lesson_id, title)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_homework_tasks_homework_id_order_index__active
    ON homework_tasks (homework_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_homework_submissions_user_id_task_id__active
    ON homework_submissions (user_id, task_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_exam_sections_exam_id_order_index__active
    ON exam_sections (exam_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_exam_sections_exam_id_title__active
    ON exam_sections (exam_id, title)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_exam_tasks_section_id_order_index__active
    ON exam_tasks (section_id, order_index)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uidx_product_registrations_user_id_product_id__active
    ON product_registrations (user_id, product_id)
    WHERE deleted_at IS NULL
    AND status = 'ACTIVE';

CREATE UNIQUE INDEX uidx_certificates_certificate_code__active
    ON certificates (certificate_code)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_products_category_id
    ON products (category_id);

CREATE INDEX idx_certificates_user_id
    ON certificates (user_id);

CREATE INDEX idx_certificates_content_item_id
    ON certificates (content_item_id);

CREATE INDEX idx_exam_sections_exam_id
    ON exam_sections (exam_id);

CREATE INDEX idx_exam_submissions_task_id
    ON exam_submissions (task_id);

CREATE INDEX idx_exam_submissions_user_id
    ON exam_submissions (user_id);

CREATE INDEX idx_homework_submissions_task_id
    ON homework_submissions (task_id);

CREATE INDEX idx_homework_submissions_user_id
    ON homework_submissions (user_id);

CREATE INDEX idx_lesson_attendances_lesson_id
    ON lesson_attendances (lesson_id);

CREATE INDEX idx_lesson_attendances_user_id
    ON lesson_attendances (user_id);

CREATE INDEX idx_product_content_items_content_item_id
    ON product_content_items (content_item_id);

CREATE INDEX idx_product_registrations_transaction_id
    ON product_registrations (transaction_id);

CREATE INDEX idx_content_item_translations_code
    ON content_item_translations (code);

CREATE INDEX idx_remember_me_tokens_expires_at
    ON remember_me_tokens (expires_at);

CREATE INDEX idx_remember_me_tokens_user_id
    ON remember_me_tokens (user_id);

CREATE INDEX idx_transactions_product_id
    ON transactions (product_id);

CREATE INDEX idx_transactions_user_id
    ON transactions (user_id);