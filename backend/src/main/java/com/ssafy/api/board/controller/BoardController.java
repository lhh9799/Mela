package com.ssafy.api.board.controller;

import com.ssafy.api.board.request.BoardGetListReq;
import com.ssafy.api.board.request.BoardRegisterPostReq;
import com.ssafy.api.board.request.BoardUpdatePutReq;
import com.ssafy.api.board.request.CommentRegisterPostReq;
import com.ssafy.api.board.response.BoardRes;
import com.ssafy.api.board.response.CommentRes;
import com.ssafy.api.board.service.BoardService;
import com.ssafy.api.user.service.UserService;
import com.ssafy.common.auth.SsafyUserDetails;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.Board;
import com.ssafy.db.entity.Comment;
import com.ssafy.db.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(value = "게시판 API", tags = {"Board"})
@RestController
@RequestMapping("/api/v1/board")
public class BoardController {

    @Autowired
    BoardService boardService;
    @Autowired
    UserService userService;

    @GetMapping("")
    @ApiOperation(value = "게시글 리스트 조회", notes = "<string>페이지번호, 페이지당 글 수, 검색어, 정렬조건</string>에 따라 게시글을 조회한다.")
//    public ResponseEntity<List<Board>> getBoardList(@QueryStringArgResolver BoardGetListReq listInfo) {
//    public ResponseEntity<List<Board>> getBoardList(@ModelAttribute BoardGetListReq listInfo) {
    public ResponseEntity<List<Board>> getBoardList(
            @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String word, @RequestParam(required = false) String sortKey
    ) {
//        System.out.println("getBoardList");
//        System.out.println(pgno + " " + spp + " " + word + " " + sortKey);
        BoardGetListReq boardGetListReq = new BoardGetListReq();
        boardGetListReq.setPage(page);
        boardGetListReq.setSize(size);
        boardGetListReq.setWord(word);
        boardGetListReq.setSortKey(sortKey);

        List<Board> boards = boardService.getBoardList(boardGetListReq);

        return ResponseEntity.status(200).body(null);
    }

    @PostMapping("")
    @ApiOperation(value = "게시글 등록", notes = "<string></strong>게시글을 등록한다.")
    public ResponseEntity<? extends BaseResponseBody> registerBoard(
            @ApiIgnore Authentication authentication,
            @RequestBody BoardRegisterPostReq registInfo) {
        SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();
        String userEmail = userDetails.getUsername();
        User user = userService.getUserByEmail(userEmail);

        try{
            boardService.registBoard(registInfo, user);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(BaseResponseBody.of(404, "not found"));
        }

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @PutMapping("/{boardid}")
    @ApiOperation(value = "게시글 수정", notes = "게시글을 수정한다")
    public ResponseEntity<? extends BaseResponseBody> updateBoard(
            @ApiIgnore Authentication authentication,
            @PathVariable(name = "boardid") Long boardIdx,
            @RequestBody BoardUpdatePutReq updateInfo
            ){
        SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();
        String userEmail = userDetails.getUsername();
        User user = userService.getUserByEmail(userEmail);

        boardService.updateBoard(updateInfo, boardIdx, user.getUserIdx());

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }


    @DeleteMapping("/{boardid}")
    @ApiOperation(value = "게시글 삭제", notes = "<string>게시글 아이디로</strong>로 댓글을 삭제한다.")
    public ResponseEntity<? extends BaseResponseBody> deleteBoard(
            @PathVariable(name = "boardid") Long boardIdx
    ) {
        boardService.deleteBoard(boardIdx);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @GetMapping("/{boardid}")
    @ApiOperation(value = "게시글 단건 조회", notes = "<string>게시글 아이디</string>로 게시글을 조회한다.")
    public ResponseEntity<BoardRes> getBoard(@PathVariable(name = "boardid") Long boardIdx) {
        try {
            Board board = boardService.getBoard(boardIdx);
            System.out.println("board: "+board.getBoardIdx());

            return ResponseEntity.status(200).body(BoardRes.of(board));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }

    }


    @PostMapping("/{boardid}/comments")
    @ApiOperation(value = "댓글 등록", notes = "<string></strong>댓글을 등록한다.")
    public ResponseEntity<? extends BaseResponseBody> registerComment(
            @ApiIgnore Authentication authentication,
            @RequestBody CommentRegisterPostReq registInfo,
            @PathVariable(name = "boardid") Long boardIdx) {
        SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();
        String userEmail = userDetails.getUsername();
        User user = userService.getUserByEmail(userEmail);

        try{
            boardService.registComment(registInfo, boardService.getBoard(boardIdx), user);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(BaseResponseBody.of(404, "not found"));
        }

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @GetMapping("/{boardid}/comments")
    @ApiOperation(value = "댓글 목록 조회", notes = "<string>게시글 아이디</strong>로 댓글 목록을 불러온다.")
    public ResponseEntity<List<CommentRes>> getCommentList(
            @PathVariable(name = "boardid") Long boardId
    ) {
        List<Comment> comments = boardService.listComment(boardId);
        List<CommentRes> res = new ArrayList<>();
        for (Comment c : comments) {
            res.add(CommentRes.of(c));
        }

        return  ResponseEntity.status(200).body(res);
    }

    @DeleteMapping("/{boardid}/comments/{commentid}")
    @ApiOperation(value = "댓글 삭제", notes = "<string>댓글 아이디로</strong>로 댓글을 삭제한다.")
    public ResponseEntity<? extends BaseResponseBody> deleteComment(
            @PathVariable(name = "boardid") Long boardIdx, @PathVariable(name = "commentid") Long commentIdx
    ) {
        boardService.deleteComment(commentIdx);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    /*
    @PostMapping()
	@ApiOperation(value = "회원 가입", notes = "<strong>아이디와 패스워드 ...를</strong>를 통해 회원가입 한다.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "성공"),
			@ApiResponse(code = 401, message = "인증 실패"),
			@ApiResponse(code = 404, message = "사용자 없음"),
			@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseResponseBody> register(
			@RequestBody @ApiParam(value="회원가입 정보", required = true) UserRegisterPostReq registerInfo) {

		//임의로 리턴된 User 인스턴스. 현재 코드는 회원 가입 성공 여부만 판단하기 때문에 굳이 Insert 된 유저 정보를 응답하지 않음.
		User user = userService.createUser(registerInfo);
		user.setUserType("unauth");

		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
	}
     */

}
