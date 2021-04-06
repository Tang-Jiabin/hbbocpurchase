// JavaScript Document
  function pros_hover(g) {
                    document.getElementById("pros01").style.display = (g == 1 ? "block" : "none");
                    document.getElementById("pros02").style.display = (g == 2 ? "block" : "none");

                    document.getElementById("ps01").className = (g == 1 ? "a_h" : "");
                    document.getElementById("ps02").className = (g == 2 ? "a_h" : "");


                }
				
				$(document).ready(function(){
	$(".tab a").click(function(){
		$(".tab a").removeClass("sel");
		$(this).addClass("sel");
	});
});