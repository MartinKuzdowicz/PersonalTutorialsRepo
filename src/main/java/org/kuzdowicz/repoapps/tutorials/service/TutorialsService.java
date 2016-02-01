package org.kuzdowicz.repoapps.tutorials.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuzdowicz.repoapps.tutorials.constants.AppFromatters;
import org.kuzdowicz.repoapps.tutorials.dao.TutorialsDao;
import org.kuzdowicz.repoapps.tutorials.model.Tutorial;
import org.kuzdowicz.repoapps.tutorials.model.TutorialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TutorialsService {

	@Autowired
	private TutorialsDao tutorialsDao;

	@Autowired
	private TutorialsCategoriesService tutorialsCategoriesService;

	public List<Tutorial> selectAll() {

		return tutorialsDao.getAllTutorials();

	}

	public Tutorial getOneById(Long id) {

		return tutorialsDao.getOneById(id);

	}

	public void removeOneById(Long id) {

		tutorialsDao.deleteTutorial(getOneById(id));

	}

	public void saveOrUpdateTutorialByPostReq(Map<String, String> reqParamsMap) {

		String categoryName = reqParamsMap.get("category");

		Tutorial newTutorial = new Tutorial();

		// BASIC DATA
		newTutorial.setAuthor(reqParamsMap.get("author"));
		newTutorial.setTitle(reqParamsMap.get("title"));
		newTutorial.setUrl(reqParamsMap.get("url"));
		newTutorial.setUrl(reqParamsMap.get("serviceDomain"));

		// RATING
		String rating = reqParamsMap.get("rating");
		String checkedRating = Optional.ofNullable(rating).filter(val -> StringUtils.isNoneBlank(rating)).orElse("0");
		newTutorial.setRating(Long.parseLong(checkedRating));

		// WORK DONE
		String reworkedInPercents = reqParamsMap.get("reworkedInPercents");
		String checkedReworkedinPercent = Optional.ofNullable(reworkedInPercents)
				.filter(val -> StringUtils.isNoneBlank(val)).orElse("0");
		newTutorial.setReworkedInPercents(Integer.parseInt(checkedReworkedinPercent));

		// WHEN TO DO
		String startDateToDoStr = reqParamsMap.get("startDateToDo");
		// DEFAULT TODAY
		String defaultStartDate = DateTime.now().toString(AppFromatters.DATE_TIME_FORMATTER);

		@SuppressWarnings("deprecation")
		String startDateToDoStrSanitized = com.google.common.base.Objects.firstNonNull(startDateToDoStr,
				defaultStartDate);

		String endDateToDoStr = reqParamsMap.get("endDateToDoStr");
		// DEFALUT PLUS ONE WEEK
		String defaultendDateToDo = DateTime.now().plusWeeks(1).toString(AppFromatters.DATE_TIME_FORMATTER);

		@SuppressWarnings("deprecation")
		String endDateToDoStrSanitized = com.google.common.base.Objects.firstNonNull(endDateToDoStr,
				defaultendDateToDo);

		newTutorial
				.setStartDateToDo(AppFromatters.DATE_TIME_FORMATTER.parseDateTime(startDateToDoStrSanitized).toDate());
		newTutorial.setEndDateToDo(AppFromatters.DATE_TIME_FORMATTER.parseDateTime(endDateToDoStrSanitized).toDate());

		// SAVE OR UPDATE
		tutorialsDao.saveOrUpdateTutorial(newTutorial);

		TutorialCategory cat = tutorialsCategoriesService.getOneByName(categoryName);

		if (Optional.ofNullable(cat).isPresent()) {
			cat.getTutorials().add(newTutorial);
		} else {
			cat = new TutorialCategory();
			cat.setCategoryName(categoryName);
			cat.setTutorials(new ArrayList<>());
			cat.getTutorials().add(newTutorial);
		}

		tutorialsCategoriesService.insertOrUpdate(cat);

	}

}
