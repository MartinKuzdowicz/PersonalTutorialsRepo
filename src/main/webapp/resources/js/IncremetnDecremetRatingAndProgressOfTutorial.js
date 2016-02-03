$(document)
		.ready(
				function() {

					var incrementRatingBtn = $('.incrementRatingBtn');

					var decrementRatingBtn = $('.decrementRatingBtn');

					var incrementTutorialProgressBtn = $('.incrementTutorialProgressBtn');

					var decrementTutorialProgressBtn = $('.decrementTutorialProgressBtn');

					incrementRatingBtn
							.click(function() {

								var clickedItemPk = $(this).data('item-pk');

								var fullJQueryDomSelector = '#ratingOfTutorial'
										+ clickedItemPk;

								executeAjaxPostForTutorialRating(
										'/PersonalTutorialsRepo/tutorial-rating-increment',
										fullJQueryDomSelector);

							})

					decrementRatingBtn
							.click(function() {
								var clickedItemPk = $(this).data('item-pk');

								var fullJQueryDomSelector = '#ratingOfTutorial'
										+ clickedItemPk;

								executeAjaxPostForTutorialRating(
										'/PersonalTutorialsRepo/tutorial-rating-decrement',
										fullJQueryDomSelector);

							})

					incrementTutorialProgressBtn
							.click(function() {
								var clickedItemPk = $(this).data('item-pk');

								var fullJQueryDomSelector = '#progressOfTutorial'
										+ clickedItemPk;

								executeAjaxPostForTutorialProgress(
										'/PersonalTutorialsRepo/tutorial-progress-increment',
										fullJQueryDomSelector);

							})

					decrementTutorialProgressBtn
							.click(function() {

								var clickedItemPk = $(this).data('item-pk');

								var fullJQueryDomSelector = '#progressOfTutorial'
										+ clickedItemPk;

								executeAjaxPostForTutorialProgress(
										'/PersonalTutorialsRepo/tutorial-progress-decrement',
										fullJQueryDomSelector);
							})

				});

function executeAjaxPostForTutorialRating(urlString, domSelector) {

	var pkFromSelector = domSelector.replace(/[#A-Za-z]/g, '');

	$.ajax({
		url : urlString,
		type : 'POST',
		data : {
			id : pkFromSelector
		},
		success : function(tutorialDTO) {

			console.log(tutorialDTO);

			$(domSelector).text(tutorialDTO.rating);

		},
		error : function(request, status, error) {
			alert(request.responseText);
		}

	});
}

function executeAjaxPostForTutorialProgress(urlString, domSelector) {

	var pkFromSelector = domSelector.replace(/[#A-Za-z]/g, '');

	$.ajax({
		url : urlString,
		type : 'POST',
		data : {
			id : pkFromSelector
		},
		success : function(tutorialDTO) {

			console.log(tutorialDTO);

			$(domSelector).text(tutorialDTO.progress);

		},
		error : function(request, status, error) {
			alert(request.responseText);
		}

	});
}