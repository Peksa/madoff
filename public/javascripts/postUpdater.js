/**
 * @param jsonList - a list of json post structures defining the posts to update, or empty list
 * @param templateBuilder - a callback function building html from a single json post
 * @param parentContainer - id of element to attach finished template to
 * @param timestamp - the timestamp of the previously latest added post
 * @param fadeIn - true if new elements should fade in
 */
function attachPosts(jsonList, templateBuilder, parentContainer, timestamp, fadeIn) {
	var lastTimestamp = timestamp;
	$.each(jsonList, function(index,comment) {
		if(comment.timestamp >= lastTimestamp) {
			lastTimestamp = Math.max(lastTimestamp,comment.timestamp);
		
			var template = templateBuilder(comment);
		
			if(fadeIn) {
				$(template).appendTo(parentContainer);
			} else {
				$(template)
				.hide()
				.appendTo(parentContainer)
				.fadeIn();
			}
		}
		
		//$('#comments').scrollBottom();
	});
	
	return lastTimestamp
}