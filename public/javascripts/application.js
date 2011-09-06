$(document).ready(function(){  
  $("body").bind("click", function (e) {
    $('.dropdown-toggle, .menu').parent("li").removeClass("open");
  });
  $(".dropdown-toggle, .menu").click(function (e) {
    var $li = $(this).parent("li").toggleClass('open');
    return false;
  });
  
  $(".close").click(function (e) {
	    var $li = $(this).parent("div").slideUp();
	    return false;
  });
	  
});

