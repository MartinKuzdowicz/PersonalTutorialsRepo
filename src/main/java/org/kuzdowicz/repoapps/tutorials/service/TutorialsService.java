package org.kuzdowicz.repoapps.tutorials.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.kuzdowicz.repoapps.tutorials.constants.AppFromatters;
import org.kuzdowicz.repoapps.tutorials.dao.TutorialsDao;
import org.kuzdowicz.repoapps.tutorials.model.Tutorial;
import org.kuzdowicz.repoapps.tutorials.model.TutorialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;

@Service
public class TutorialsService {

	@Autowired
	private TutorialsDao tutorialsDao;

	@Autowired
	private TutorialsCategoriesService tutorialsCategoriesService;

	private final static Range<Integer> rangeForProgressIncrementation = Range.closed(0, 95);

	private final static Range<Integer> rangeForProgressDecrementation = Range.closed(5, 100);

	public List<Tutorial> selectAll() {

		return tutorialsDao.getAllTutorials();

	}

	public Tutorial getOneById(Long id) {

		return tutorialsDao.getOneById(id);

	}

	public void removeOneById(Long id) {

		tutorialsDao.deleteTutorial(getOneById(id));

	}

	public Tutorial incremetRatingAndReturnChangedObject(Long pk) {

		Tutorial tutorial = getOneById(pk);
		Long actualRating = tutorial.getRating();

		Optional.of(actualRating).filter(val -> val < Long.MAX_VALUE).ifPresent(presentVal -> {

			presentVal++;
			tutorial.setRating(presentVal);
			tutorialsDao.saveOrUpdateTutorial(tutorial);

		});

		return tutorial;

	}

	public Tutorial decrementRatingAndReturnChangedObject(Long pk) {

		Tutorial tutorial = getOneById(pk);
		Long actualRating = tutorial.getRating();

		Optional.of(actualRating).filter(val -> val > 0).ifPresent(presentVal -> {

			presentVal--;
			tutorial.setRating(presentVal);
			tutorialsDao.saveOrUpdateTutorial(tutorial);

		});

		return tutorial;

	}

	public Tutorial incremetTutorialProgressAndReturnChangedObject(Long pk) {

		Tutorial tutorial = getOneById(pk);
		Integer actualProgress = tutorial.getProgress();

		Optional.of(actualProgress).filter(val -> rangeForProgressIncrementation.contains(val))
				.ifPresent(presentValue -> {

					presentValue += 5;
					tutorial.setProgress(presentValue);
					tutorialsDao.saveOrUpdateTutorial(tutorial);

				});

		return tutorial;

	}

	public Tutorial decremetTutorialProgressAndReturnChangedObject(Long pk) {

		Tutorial tutorial = getOneById(pk);
		Integer actualProgress = tutorial.getProgress();

		Optional.of(actualProgress).filter(val -> rangeForProgressDecrementation.contains(val))
				.ifPresent(presentValue -> {

					presentValue -= 5;
					tutorial.setProgress(presentValue);
					tutorialsDao.saveOrUpdateTutorial(tutorial);

				});

		return tutorial;

	}

	public List<Tutorial> getTutorialsToDoForCurrentWeekWithDaysLeftFiled() {

		Date firstDayOfCurrentWeek = DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY).toDate();
		Date weekLater = DateTime.now().plusWeeks(1).toDate();

		List<Tutorial> allTutorialsBeetwenGivenDates = tutorialsDao
				.getAllTutorialsBeetwenGivenDates(firstDayOfCurrentWeek, weekLater);

		allTutorialsBeetwenGivenDates.forEach(tutorial -> {

			DateTime tutorialEndDateJodaVar = new DateTime(tutorial.getEndDateToDo());
			DateTime today = DateTime.now();

			// MINUS 1 DAY FROM TODAY AND PLUS 1 DAY TO END DATE
			// BEACOUSE THIS IS 2 DAYS OF DIFERENCE
			// JODA FUNCTION RETURN DAYS BEETWEN EXLUDE GIVEN DAYS

			Days days = Days.daysBetween(today.minusDays(1), tutorialEndDateJodaVar.plusDays(1));

			Integer daysLeft = Optional.of(days.getDays()).filter(d -> d > 0).orElse(0);

			tutorial.setDaysLeft(new Long(daysLeft));

		});

		return allTutorialsBeetwenGivenDates;
	}

	private String extractDomainAddresFromFullUrl(String urlStr) {
		String urlStrSanitized = urlStr.replace("http://", "").replace("https://", "");

		int beginIndex = urlStrSanitized.indexOf("www");

		if (beginIndex == -1) {
			beginIndex = 0;
		}

		return urlStrSanitized.substring(beginIndex, urlStrSanitized.indexOf("/", beginIndex));
	}

	public void saveOrUpdateTutorialByPostReq(Map<String, String> reqParamsMap) {

		String categoryName = reqParamsMap.get("category");

		Tutorial newTutorial = new Tutorial();

		// BASIC DATA
		newTutorial.setAuthor(reqParamsMap.get("author"));
		newTutorial.setTitle(reqParamsMap.get("title"));
		newTutorial.setUrl(reqParamsMap.get("url"));

		Optional.ofNullable(reqParamsMap.get("url")).filter(url -> StringUtils.isNoneBlank(url)).ifPresent(url -> {

			String urlStr = reqParamsMap.get("url");

			String serviceWebAddres = extractDomainAddresFromFullUrl(urlStr);

			newTutorial.setServiceDomain(serviceWebAddres);

		});

		String idFromReq = reqParamsMap.get("id");

		if (StringUtils.isNoneBlank(idFromReq)) {
			newTutorial.setId(Long.parseLong(idFromReq));
		}

		// RATING
		String rating = reqParamsMap.get("rating");
		String checkedRating = Optional.ofNullable(rating).filter(val -> StringUtils.isNoneBlank(rating)).orElse("0");
		newTutorial.setRating(Long.parseLong(checkedRating));

		// WORK DONE
		String reworkedInPercents = reqParamsMap.get("reworkedInPercents");
		String checkedReworkedinPercent = Optional.ofNullable(reworkedInPercents)
				.filter(val -> StringUtils.isNoneBlank(val)).orElse("0");
		newTutorial.setProgress(Integer.parseInt(checkedReworkedinPercent));

		// WHEN TO DO
		String startDateToDoStr = reqParamsMap.get("startDateToDo");
		// DEFAULT TODAY
		String defaultStartDate = DateTime.now().toString(AppFromatters.DATE_TIME_FORMATTER);

		String startDateToDoStrSanitized = Optional.of(startDateToDoStr)
				.filter(startDate -> StringUtils.isNoneBlank(startDate)).orElse(defaultStartDate);

		String endDateToDoStr = reqParamsMap.get("endDateToDo");
		// DEFALUT PLUS ONE WEEK
		String defaultendDateToDo = DateTime.now().plusWeeks(1).toString(AppFromatters.DATE_TIME_FORMATTER);

		String endDateToDoStrSanitized = Optional.of(startDateToDoStr)
				.filter(startDate -> StringUtils.isNoneBlank(endDateToDoStr)).orElse(defaultendDateToDo);

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
