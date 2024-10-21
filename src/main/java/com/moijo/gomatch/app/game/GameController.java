package com.moijo.gomatch.app.game;

import com.moijo.gomatch.domain.game.service.GameService;
import com.moijo.gomatch.domain.game.vo.GameVO;
import com.moijo.gomatch.domain.game.vo.StadiumVO;
import com.moijo.gomatch.domain.member.vo.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GameController {

    @Autowired
    private GameBatchComponent gameBatchComponent;

    @Autowired
    private GameService gameService;

    @GetMapping("/game/checkLogin")
    @ResponseBody
    public Map<String, Boolean> checkLogin(HttpSession session) {
        Map<String, Boolean> response = new HashMap<>();
        String memberId = (String) session.getAttribute("memberId");
        response.put("loggedIn", memberId != null);
        return response;
    }

    @ModelAttribute
    public void addAttributes(Model model, HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("member");
        if (member != null) {
            model.addAttribute("loggedIn", true);
            model.addAttribute("memberNickName", member.getMemberNickName());
        } else {
            model.addAttribute("loggedIn", false);
        }
    }

    // 팀 순위 보여주는 메소드
    @GetMapping("/game/teamrank")
    public String showTeamRankPage(@RequestParam(value = "year", required = false, defaultValue = "2024") String year, Model model, HttpSession session) {
        List<String[]> teamList = gameBatchComponent.scrapeTeamRank(year);
        if ("2024".equals(year)) {
            session.setAttribute("2024_Rank", teamList);
        }
        model.addAttribute("teams", teamList);
        model.addAttribute("selectedYear", year);
        return "game/teamRankPage";
    }

    // 선수 순위 보여주는 메소드
    @GetMapping("/game/playerrank")
    public String showPlayerRankPage(@RequestParam(value = "year", required = false, defaultValue = "2024") String year, Model model) {
        List<String[]> pitcherList = gameBatchComponent.scrapePitcherRank(year);
        model.addAttribute("pitchers", pitcherList);
        model.addAttribute("selectedYear", year);
        return "game/playerRankPage";
    }

    // 경기 일정 보여주는 메소드
    @GetMapping("/game/list")
    public String showGameListPage(HttpSession session, @RequestParam(value = "month", required = false, defaultValue = "10") String month, Model model) {
        //String memberId = (String) session.getAttribute("memberId");
        String preferenceClub = (String) session.getAttribute("preferenceClub");
        String year = "2024";
        String dateParam = year + month;
        // 크롤링 한 데이터로부터 gameList 리스트 생성
        List<GameVO> gameList = gameBatchComponent.scrapeSchedule(dateParam);
        model.addAttribute("games", gameList);  // gameList를 games라는 이름으로 페이지에 저장
        model.addAttribute("selectedMonth", month);
        model.addAttribute("preferenceClub", preferenceClub);
        return "game/listPage";
    }

    // 야구장 날씨 보여주는 메소드
    @GetMapping("/game/weather")
    public String getStadiums(Model model) {
        List<StadiumVO> stadiumList = gameService.getAllStadiums();
        model.addAttribute("stadiums", stadiumList);
        return "game/weatherPage";
    }
}
