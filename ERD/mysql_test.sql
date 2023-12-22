-- 기존테이블 삭제
DELETE FROM hot_tour_recommend;
DELETE FROM hot_camping_recommendcount;
DELETE FROM hot_post;
ALTER TABLE hot_post AUTO_INCREMENT = 1;
DELETE FROM hot_user_role;
ALTER TABLE hot_user_role AUTO_INCREMENT = 1;
DELETE FROM hot_role;
ALTER TABLE hot_role AUTO_INCREMENT = 1;
DELETE FROM hot_user ;
ALTER TABLE hot_user AUTO_INCREMENT = 1;


-- 샘플 authority
INSERT INTO hot_role (role_name) VALUES
                                    ('ROLE_MEMBER'), ('ROLE_ADMIN');


INSERT INTO hot_user (username,nickname, password,email) VALUES
                                                          ('USER1', '회원1','$2a$10$6gVaMy7.lbezp8bGRlV2fOArmA3WAk2EHxSKxncnzs28/m3DXPyA2', 'user1@mail.com'),
                                                          ('USER2', '회원2','$2a$10$7LTnvLaczZbEL0gabgqgfezQPr.xOtTab2NAF/Yt4FrvTSi0Y29Xa',  'user2@mail.com'),
                                                          ('ADMIN1', '관리자1','$2a$10$53OEi/JukSMPr3z5RQBFH.z0TCYSUDPtxf1/8caRyRVdDNdHA9QHi',  'admin1@mail.com'),
                                                          ('USER3', '회원3','$2a$10$53OEi/JukSMPr3z5RQBFH.z0TCYSUDPtxf1/8caRyRVdDNdHA9QHi',  'user31@mail.com')
;


-- 샘플 사용자-권한
INSERT INTO hot_user_role VALUES
                                    (1, 1),
                                    (1, 3),
                                    (2, 3),
                                    (1, 4)
;
select * from hot_post;

-- 샘플 글
INSERT INTO hot_post (userId,  tour_id,  camping_id, category, subject ,content,title,img,visibility ) VALUES
                                                    (1, 2, '0',14,'sss','asdfasf','투어제목','사진','PUBLIC'),
                                                    (2, '0', 1,15,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (1, 2, '0',28,'sss','asdfasf','투어제목','사진','PUBLIC'),
                                                    (2, '0', 1,32,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (2, '0', 1,38,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (2, '0', 1,39,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (2, '0', 1,28,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (2, '0', 1,32,'111','본문','캠핑제목','사진','PUBLIC'),
                                                    (2, '0', 1,14,'111','본문','캠핑제목','사진','PUBLIC');



SELECT * FROM hot_post;
SELECT * FROM hot_user;

SHOW TABLES;


SELECT TABLE_NAME FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'db907'
  AND TABLE_NAME LIKE 'hot_%'
;


SELECT * FROM hot_role;
SELECT * FROM hot_user ORDER BY uid DESC;
SELECT * FROM hot_user_role;
SELECT * FROM hot_post ORDER BY postId DESC;


SELECT count(*) FROM hot_tour_mysql;  -- 21975
SELECT contentid FROM hot_tour_mysql;
SELECT * FROM hot_comment;

SELECT count(*) FROM hot_tour_mysql;
SELECT * FROM hot_tour_recommend;
SELECT * FROM hot_user;
SELECT * FROM hot_tour_mysql;

# DELETE FROM hot_tour_recommend;
SELECT count(*) FROM hot_tour_mysql;
SELECT FLOOR( 1 + RAND() * 4 ) "uid", FLOOR(1 + RAND() * (SELECT count(*) FROM hot_tour_mysql))  FROM hot_tour_mysql;
replace INTO hot_tour_recommend
(SELECT FLOOR( 1 + RAND() * 4 ), FLOOR(1 + RAND() * (SELECT count(*) FROM hot_tour_mysql))  FROM hot_tour_mysql);

-- 투어SELECT FLOOR( 1 + RAND( ) * 4 );
SELECT count(*) FROM hot_camping;

SELECT *
FROM hot_tour_mysql
WHERE contentid = 3056662 AND contenttypeid = 12;

SELECT * FROM hot_camping_recommendcount;
-- 캠핑
-- SELECT FLOOR( 1 + RAND( ) * 4 );
replace INTO hot_camping_recommendcount
    (SELECT FLOOR( 1 + RAND() * 4 ), FLOOR(1 + RAND() * (SELECT count(*) FROM hot_camping))  FROM hot_camping);

UPDATE hot_tour_mysql
SET viewcnt = FLOOR(RAND() * 50) + 1; -- 1부터 50까지의 랜덤 값
--
--
select * from hot_tour_mysql;
select * from hot_tour_recommend;
select * from hot_camping;
select * from hot_camping_recommendcount;
select * from hot_camping;

# 추천수 기준 정렬
SELECT
    count(r.tour_id) "count_tour",
    r.tour_id "tour_id",
    t.title "title"

FROM
    hot_tour_mysql t, hot_tour_recommend r
WHERE
    1 = 1
    AND t.tour_id = r.tour_id
    AND t.areacode = 1
    AND t.contenttypeid = 12
GROUP BY r.tour_id
ORDER BY count_tour DESC
LIMIT 100
;

# 조회수 기준 정렬
SELECT
    count(t.viewcnt) "viewcnt",
    r.tour_id "tour_id",
    t.title "title"

FROM
    hot_tour_mysql t, hot_tour_recommend r ,hot_post p
WHERE
        1 = 1
  AND t.tour_id = r.tour_id
  AND t.areacode = 1
  AND t.contenttypeid = 12
GROUP BY r.tour_id
ORDER BY viewcnt DESC
LIMIT 1000
;


SELECT COUNT(*) FROM hot_camping WHERE doNm = '강원도';
SELECT facltNm, camping_contentid
FROM hot_camping WHERE facltNm LIKE CONCAT('%', '파크킹', '%');






SELECT *
FROM hot_post
WHERE category = 12;


SELECT * FROM hot_comment;


SELECT * FROM hot_tour_mysql WHERE areacode = 5 AND contenttypeid = 32;
SELECT * FROM hot_tour_mysql WHERE areacode = 5 AND contenttypeid = 38;

SELECT * FROM hot_tour_mysql WHERE areacode = 1 AND contenttypeid = 32;


SELECT * FROM hot_tour_mysql;

select * from hot_attachment;

# SELECT title FROM hot_tour_mysql WHERE title LIKE '%' || '축제' || '%'
SELECT * FROM hot_tour_mysql WHERE title LIKE '%해운대수목원%';

SELECT title,tour_id "id" FROM hot_tour_mysql WHERE contenttypeid = 12 AND title LIKE CONCAT('%', '해운대수목원', '%');
SELECT * FROM hot_post;

INSERT INTO hot_post(userId, subject,category,visibility, content)
SELECT userId, subject,category, visibility,content FROM hot_post;
SELECT * FROM hot_post;
SELECT * FROM hot_user;
SELECT *
FROM hot_post
WHERE category = 12
ORDER BY postId DESC
LIMIT 10 OFFSET 0;

SELECT
    p.postId "p_postId",
    p.tour_id "p_tourid",
    p.camping_id "p_campingid",
    p.category "p_category",
    p.subject "p_subject",
    p.content "p_content",
    p.visibility "p_visibility",
    p.viewcnt "p_viewcnt",
    p.regDate "regDate",
    p.title "p_title",
    p.img "p_img",
    u.uid "u_uid",
    u.username "u_username",
    u.regDate "u_regDate",
    u.nickname "nickname",
    u.email "u_email"
FROM
    hot_post p, hot_user u
WHERE
        p.userId = u.uid
AND p.category = '12'
ORDER BY p.postId DESC
    LIMIT 10 OFFSET 0