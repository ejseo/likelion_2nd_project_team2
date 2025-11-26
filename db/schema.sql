CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  role VARCHAR(20) DEFAULT 'USER',
  created_at DATETIME NOT NULL,
  updated_at DATETIME
);

CREATE TABLE post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  rating INT NOT NULL,
  category VARCHAR(50) NOT NULL,
  tags VARCHAR(200),
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE INDEX idx_post_category_created_at
  ON post (category, created_at DESC);

CREATE INDEX idx_post_user
  ON post (user_id, created_at DESC);

CREATE TABLE post_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  sort_order INT DEFAULT 0,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (post_id) REFERENCES post(id)
);

CREATE TABLE comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_comment_id BIGINT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (post_id) REFERENCES post(id),
  FOREIGN KEY (user_id) REFERENCES user_account(id),
  FOREIGN KEY (parent_comment_id) REFERENCES comment(id)
);

CREATE INDEX idx_comment_post
  ON comment (post_id, created_at);

CREATE TABLE post_like (
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (post_id, user_id),
  FOREIGN KEY (post_id) REFERENCES post(id),
  FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE TABLE post_bookmark (
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (post_id, user_id),
  FOREIGN KEY (post_id) REFERENCES post(id),
  FOREIGN KEY (user_id) REFERENCES user_account(id)
);