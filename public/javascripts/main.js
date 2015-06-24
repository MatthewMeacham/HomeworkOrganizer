$( document ).ready(function() {
         $(".scroll").click(function() {
             $('html, body').animate({
                 scrollTop: $("#about").offset().top
             }, 1000);
         });
});