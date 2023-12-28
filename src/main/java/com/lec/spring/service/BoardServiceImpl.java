package com.lec.spring.service;

import com.lec.spring.domain.Attachment;
import com.lec.spring.domain.Post;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AttachmentRepository;
import com.lec.spring.repository.BoardRepository;
import com.lec.spring.repository.PostRepository;
import com.lec.spring.repository.UserRepository;
import com.lec.spring.util.U;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service
public class BoardServiceImpl implements BoardService {

    @Value("${app.upload.path}")
    private String uploadDir;

    @Value("${app.pagination.page_rows}")
    private int PAGE_ROWS;

    @Value("${app.pagination.write_pages}")
    private int WRITE_PAGES;
    private UserRepository userRepository;
    private PostRepository postRepository;

    private BoardRepository boardRepository;
    private AttachmentRepository attachmentRepository;

    @Autowired
    public BoardServiceImpl(SqlSession sqlSession){
        userRepository = sqlSession.getMapper(UserRepository.class);
        postRepository = sqlSession.getMapper(PostRepository.class);
        attachmentRepository = sqlSession.getMapper(AttachmentRepository.class);
        boardRepository = sqlSession.getMapper(BoardRepository.class);
    }

//    @Override
//    public List<Post> getTotalTourPost(String area, String areaCode, String contentTypeId, String orderby, int limit, int offset) {
//        return boardRepository.getTotalTourPost(area, areaCode, contentTypeId, orderby, limit, offset);
//    }
//
//    @Override
//    public List<Post> getTotalCampingPost(String area, String areaCode, String orderby, int limit, int offset) {
//        return boardRepository.getTotalCampingPost(area, areaCode, orderby, limit, offset);
//    }

    @Override
    public List<Post> boardSearchData(String keyword, int limit, int offset) {
        return boardRepository.boardSearch(keyword, limit, offset );
    }

    @Override
    public int write(Post post, Map<String, MultipartFile> files) {
        // 현재 로그인한 작성자 정보.
        User user = U.getLoggedUser();

        // 위 정보는 session 의 정보이고, 일단 DB 에서 다시 읽어온다
        user = userRepository.findById(user.getUid());
        post.setUser(user);   // 글 작성자 세팅

        int cnt = postRepository.save(post);

        // 첨부파일 추가
        addFiles(files, post.getPostId());

        return cnt;
    }
    // 특정 글(id) 첨부파일(들) 추가
    private void addFiles(Map<String, MultipartFile> files, Long postId) {
        if(files != null){
            for(var e : files.entrySet()){

                // name="upfile##" 인 경우만 첨부파일 등록. (이유, 다른 웹에디터와 섞이지 않도록..ex: summernote)
                if(!e.getKey().startsWith("upfile")) continue;

                // 첨부 파일 정보 출력
                System.out.println("\n첨부파일 정보: " + e.getKey());   // name값
                U.printFileInfo(e.getValue());   // 파일 정보 출력
                System.out.println();

                // 물리적인 파일 저장
                Attachment file = upload(e.getValue());

                // 성공하면 DB 에도 저장
                if(file != null){
                    file.setPostId(postId);   // FK 설정
                    attachmentRepository.saveimg(file);   // INSERT
                }
            }
        }
    } // end addFiles()

    private Attachment upload(MultipartFile multipartFile) {
        Attachment attachment = null;

        // 담긴 파일이 없으면 pass
        String originalFilename = multipartFile.getOriginalFilename();
        if(originalFilename == null || originalFilename.length() == 0) return null;

        // 원본파일명
        String sourceName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        // 저장될 파일명
        String fileName = sourceName;

        // 파일명 이 중복되는지 확인
        File file = new File(uploadDir, sourceName);
        if(file.exists()){  // 이미 존재하는 파일명,  중복되면 다름 이름으로 변경하여 저장
            // a.txt => a_2378142783946.txt  : time stamp 값을 활용할거다!
            int pos = fileName.lastIndexOf(".");
            if(pos > -1){   // 확장자가 있는 경우
                String name = fileName.substring(0, pos);  // 파일 '이름'
                String ext = fileName.substring(pos + 1);   // 파일 '확장자'

                // 중복방지를 위한 새로운 이름 (현재시간 ms) 를 파일명에 추가
                fileName = name + "_" + System.currentTimeMillis() + "." + ext;
            } else {  // 확장자가 없는 경우
                fileName += "_" + System.currentTimeMillis();
            }
        }
        // 저장할 파일명
        System.out.println("fileName: " + fileName);

        // java.nio
        Path copyOfLocation = Paths.get(new File(uploadDir, fileName).getAbsolutePath());
        System.out.println(copyOfLocation);

        try {
            // inputStream을 가져와서
            // copyOfLocation (저장위치)로 파일을 쓴다.
            // copy의 옵션은 기존에 존재하면 REPLACE(대체한다), 오버라이딩 한다

            Files.copy(
                    multipartFile.getInputStream(),
                    copyOfLocation,
                    StandardCopyOption.REPLACE_EXISTING    // 기존에 존재하면 덮어쓰기
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        attachment = Attachment.builder()
                .filename(fileName)   // 저장된 이름
                .sourcename(sourceName)  // 원본 이름
                .build();

        return attachment;
    }
    @Override
    @Transactional
    public Post detail(Long postId) {
        postRepository.incViewCnt(postId);
        Post post = postRepository.findById(postId);

        if (post != null){
            List<Attachment> fileList = attachmentRepository.findByPost(post.getPostId());
            setImagea(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);
        }
        return post;
    }
    private void setImagea(List<Attachment> fileList) {
        // upload 실제 물리적인 경로
        String realPath = new File(uploadDir).getAbsolutePath();

        for(var attachment : fileList){
            BufferedImage imgData = null;
            File f = new File(realPath, attachment.getFilename());  // 저장된 첨부파일에 대한 File 객체

            try {
                imgData = ImageIO.read(f);
            } catch (IOException e) {
                System.out.println("파일존재안함: " + f.getAbsolutePath() + "[" + e.getMessage() + "]");
                throw new RuntimeException(e);
            }

            if(imgData != null) attachment.setImage(true);  // 이미지 여부 체크!
        }
    }
    @Override
    public List<Post> list() {
        return postRepository.findAll();
    }


    // 페이징 리스트
    @Override
    public List<Post> list(String category, Integer page, Model model) {
        // 현재 페이지 parameter
        if (page == null) page = 1;  // 디폴트는 1page
        if (page < 1) page = 1;

        // 세션에서 페이징 설정 가져오기
        HttpSession session = U.getSession();
        Integer writePages = (Integer) session.getAttribute("writePages");
        if (writePages == null) writePages = WRITE_PAGES;  // 세션에 없으면 기본값 사용
        Integer pageRows = (Integer) session.getAttribute("pageRows");
        if (pageRows == null) pageRows = PAGE_ROWS;  // 세션에 없으면 기본값 사용

        // 카테고리별 게시글 수 계산
        long cnt = postRepository.countByCategory(category);  // 변경된 부분: 카테고리별 게시글 수
        int totalPage = (int) Math.ceil((double) cnt / pageRows);   // 총 페이지 수 계산

        // 페이징 계산
        int startPage, endPage;
        List<Post> list = null;
        if (cnt > 0) {
            // 페이지 값 보정
            if (page > totalPage) page = totalPage;

            // 시작 데이터 번호
            int fromRow = (page - 1) * pageRows;

            // 페이징에 표시될 시작, 끝 페이지 번호 계산
            startPage = (((page - 1) / writePages) * writePages) + 1;
            endPage = startPage + writePages - 1;
            if (endPage > totalPage) endPage = totalPage;

            // 해당 페이지의 게시글 리스트 조회
            list = postRepository.selectFromRow(category, fromRow, pageRows);
            model.addAttribute("list", list);
        } else {
            page = 0;
            startPage = 0;
            endPage = 0;
        }

        // 모델 속성 추가
        model.addAttribute("cnt", cnt);  // 전체 글 개수
        model.addAttribute("page", page); // 현재 페이지
        model.addAttribute("totalPage", totalPage);  // 총 페이지 수
        model.addAttribute("pageRows", pageRows);  // 페이지당 게시글 수
        model.addAttribute("startPage", startPage);  // 페이징 시작 페이지
        model.addAttribute("endPage", endPage);   // 페이징 끝 페이지
        model.addAttribute("url", U.getRequest().getRequestURI());  // 현재 요청 URI

        return list;
    }




    @Override
    public Post selectById(Long postId) {
        Post post = postRepository.findById(postId);

        if(post != null){
            // 첨부파일 정보 가져오기
            List<Attachment> fileList = attachmentRepository.findByPost(post.getPostId());
            setImagea(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);

        }

        return post;
    }

    @Override
    public int update(Post post, Map<String, MultipartFile> files, Long[] delfile) {
        int result = postRepository.update(post);

        // 새로운 첨부파일 추가
        addFiles(files, post.getPostId());

        // 삭제할 첨부파일(들) 삭제
        if(delfile != null){
            for(Long fileId : delfile){
                Attachment file = attachmentRepository.findById(fileId);
                if(file != null){
                    delFile(file);   // 물리적으로 파일 삭제
                    attachmentRepository.delete(file);   // DB 에서 삭제
                }
            }
        }

        return result;
    }

    // 특정 첨부파일9id) 를 물리적으로 삭제
    private void delFile(Attachment file) {
        String saveDirectory = new File(uploadDir).getAbsolutePath();
        File f = new File(saveDirectory, file.getFilename());  // 물리적으로 저장된 파일들이 삭제 대상
        System.out.println("삭제시도--> " + f.getAbsolutePath());

        if(f.exists()){
            if(f.delete()){
                System.out.println("삭제 성공");
            } else {
                System.out.println("삭제 실패");
            }
        } else {
            System.out.println("파일이 존재하지 않습니다.");
        }
    }

    @Override
    public int deleteById(Long postId) {
        int result = 0;
        Post post = postRepository.findById(postId);  // 존재하는 데이터인지 읽어와보기
        if(post != null){  // 존재한다면 삭제 진행.
            // 물리적으로 저장된 첨부파일(들) 삭제
            List<Attachment> fileList = attachmentRepository.findByPost(postId);
            if(fileList != null){
                for(Attachment file : fileList){
                    delFile(file);
                }
            }
            result = postRepository.delete(post);
        }
        return result;
    }


}
