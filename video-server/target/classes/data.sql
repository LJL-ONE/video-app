USE video_meeting;
INSERT INTO t_user(username,password) VALUES ('test','123456') ON DUPLICATE KEY UPDATE username=username;