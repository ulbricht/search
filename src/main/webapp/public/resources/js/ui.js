/******************
 * General setup functions loaded only once
 *******************/

function init() {
	// use correct array serialization for solr
	jQuery.ajaxSettings.traditional = true; 
	jQuery.support.cors = true;
	
	options.init();

	setup_debug_box();
	setup_query_form();
	setup_history();
	
	pagination.init();
	dialog.init();
	
	process_results();
	
	$(document).delegate("span.abstract","click", function(){	
		$(this).find("span.abstractmore").toggle();		
	});
	
	// fix for #57
	if (History.getHash())
		History.Adapter.trigger(window,'statechange');
}




function setupsearchmap(){
	
	$('.searchmap').each(function(idx, elem){


		var minlat=maxlat=minlon=maxlon=false;
		$('.filter').each(function(idx,e){

			if ($(e).find('.name').text()=='geo'){
				var val=$(e).find('.value').text().trim();
				var m=val.match(/\[(.+),(.+)\ TO\ (.+),(.+)\]/);
				if (m) {
					minlat=m[1];maxlat=m[3];
					minlon=m[2];maxlon=m[4];
					$('#minlat').val(m[1]);
					$('#maxlat').val(m[3]);
					$('#minlon').val(m[2]);
					$('#maxlon').val(m[4]);

				}				
			}



		});
	

		//setup map & draw layer
		var map=L.map(elem,{ zoomControl:false }).setView([0,0],0);	
		L.tileLayer( 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
		    subdomains: ['a','b','c']
		}).addTo( map );


		var drawnItems = new L.FeatureGroup();
		map.addLayer(drawnItems);

		//draw bounds from query string
		if (minlat!=false && maxlat!=false && minlon!=false && maxlon!=false){
			var bounds = [[minlat, minlon], [maxlat, maxlon]];
			var rect=L.rectangle(bounds).addTo(map);
			drawnItems.addLayer(rect);
		}

		//setup draw control
		var drawControl = new L.Control.Draw({
			position: 'topright',
			draw: {
				polygon: false,
				polyline: false,
				rect: {
					shapeOptions: {
						color: 'red'
					},
				},
				circle: false,
				marker: false,
			},
			edit: false
		});
		map.addControl(drawControl);

		map.on('draw:created', function (e) {

			var layer = e.layer;
			var bounds=layer.getBounds();
			$('#minlat').val(bounds.getSouth());
			$('#maxlat').val(bounds.getNorth());
			$('#minlon').val(bounds.getWest());
			$('#maxlon').val(bounds.getEast());

			drawnItems.clearLayers();
			drawnItems.addLayer(layer);
submit_query();
		});
	});	

}


function setupmap(){
	
	$('.map').not('.map-processed').each(function(idx, elem){	

		$(elem).addClass("map-processed");  //do this once
		
		var data=JSON.parse($(elem).attr("data"));
		
		if (data.length<1){
			$(elem).hide();
			$(elem).closest(".doc").css("padding-bottom","20px");			
			$(elem).closest(".doc").css("min-height","0px");
			return;
		}
		
		var map=L.map(elem,{ zoomControl:false }).setView([0,0],0);	
		L.tileLayer( 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
		    subdomains: ['a','b','c']
		}).addTo( map );
		
		$.each(data, function(idx,box){
			if (box.length==2){ //marker
				var lat=box[1];
				var lon=box[0];		
				L.marker([lat,lon]).addTo(map);
			}else if (box.length==4){ //bbox+marker
				var minlat=box[1];
				var maxlat=box[3];
				var minlon=box[0];
				var maxlon=box[2];
				
				var lat=minlat+(maxlat-minlat)/2;
				var lon=minlon+(maxlon-minlon)/2;
				if (maxlat-minlat<4 && maxlon-minlon<4)
					L.marker([lat,lon]).addTo(map);
				L.polygon([[minlat,minlon],[minlat,maxlon],[maxlat,maxlon],[maxlat,minlon]]).addTo(map);
			}
		});
	
	});	

}

function setupabstract(){
	$('.abstract').not('.abstract-processed').each(function(idx, elem){	

		$(elem).addClass("abstract-processed"); //do this once
		
		var data=$(elem).text();
		if (data.length<600)
			return;
		var visible=data.substring(0,600);
		var hidden=data.substring(600);
		var html=visible+'<span class="abstractmore" style="color:#0084B9">... more...</span><span class="abstractmore" style="display:none">'+hidden+'</span>';
		$(elem).html(html);

	});		
	
}



Dialog = function() {
	this.div = $("<div>");
	this.open = function() {
		if (this.div.is(':empty')) 
			this.load();
		this.div.dialog("open");
		this.div.dialog("open");
	};
}

dialog = {
	init : function() {
		$("#link_help").click(function() { dialog.help.open(); return false; });
		$("#link_advanced").click(function() { dialog.advanced.open(); return false; });
	}
}

dialog.help = new Dialog;
dialog.help.load = function() { 
	this.div.load_sync("help.html #content").modalDialog("Metadata Search Help");
};

dialog.advanced = new Dialog;
dialog.advanced.load = function() {
	this.div.load_sync("ui-advanced form").modalDialog("Metadata Advanced Search", true, true);
	advanced.init_for_dialog();
};


var timeout_instant_search;
function setup_query_form() {
	$("#query_form").submit(function() {
		clearTimeout(timeout_instant_search);
		submit_query();
		return false;
	});

	$("#query_input").inputChange(function() {
		if (options.get("instant")) {
			clearTimeout(timeout_instant_search);
			timeout_instant_search = setTimeout(submit_query,1000);
		};
	});
	$("#query_input").focus();
}

function setup_debug_box() {
	var div = $("<div>").attr("id","debug").hide();
	var reset_link = $("<a>").attr("href","#").html("reset").click(function() {
		$("#debug ul").empty();
		$("#debug").hide();
		return false;
	});
	var add_hr_link = $("<a>").attr("href","#").html("add hr").click(function() {
		debug("<hr/>");
		return false;
	});
	div.append(reset_link, "&bullet;", add_hr_link, $("<ul>"));
	$("body").prepend(div);
}

function setup_history() {
	History.Adapter.bind(window,'statechange',function() {
		if (History.skipReloadingResults) {
			History.skipReloadingResults = false;
		} else {
			var State = History.getState();
			load_results(State.url);

			var q = $("#q").text();
			$("#query_input").val(q);
		}
    });
	History.pushStateWithoutReloadingResults = function (data, title, url) {
		var old_url = History.getState().url;
		if (old_url != url) {
			History.skipReloadingResults = true;
			History.pushState(data, title, url);
		}
	}
}

/******************
 * General functions
 *******************/

function debug(html) {
	var item = $("<li>").html(html);
	$("#debug ul").append(item);
	$("#debug").show();
}

function add_topbar_link(text, hook) {
	var a = $("<a href='#'>" + text + "</a>").click(function() {
		hook();
		return false;
	});	
	$("#links").prepend(a, " | ");
}

// returns the url of the current results
function get_lens() {
	return $("#lens").attr("href");
}

function get_sort() {
     var args = document.location.search.substring(1).split('&');
     var i=0;
     for (i=0;i<args.length;i++){
         if (args[i].match(/sort=(.+)/)){
		 return "&"+args[i];
         }
     } 	
     return "";
}

function get_lens_without_q() {
	return $("#lens_without_q").attr("href");
}

// prevent history.js from prepending '/' to url if it starts with '?'
function fixUrl(url) {
	if (url.match(/^\?/))
		url = "ui" + url;
	return url;
}

function makeFooterSticky() {
	var footer = $(".footer");
	var footer2 = footer.clone();
	footer2.css("position", "fixed").css("bottom","0px").css("padding-bottom", "8px");
	footer2.insertAfter(footer);
	function refresh() {
		footer2.show();
		footer2.toggle(footer.position().top > footer2.position().top);
		footer2.width(footer.width());
	}
	$(window).scroll(refresh);
	$(window).resize(refresh);
	refresh();
}

function throbber() {
 	return $('<img src="resources/img/throbber-transparent.gif" class="loading" alt="spinning wheel"/>');
}

function geospatial(){

	var minlat=$('#minlat').val();
 	var maxlat=$('#maxlat').val();
	var minlon=$('#minlon').val();
	var maxlon=$('#maxlon').val();

	if (isNaN(parseFloat(minlat)) || isNaN(parseFloat(maxlat)) || isNaN(parseFloat(minlon)) || isNaN(parseFloat(maxlon))) 
		return "";
	else 
		return "&fq=geo:["+minlat+","+minlon+"+TO+"+maxlat+","+maxlon+"]"
}


/******************
 * functions for loading content
 ******************/

function submit_query() {
	var q = $("#query_input").val();
	if (q != "") {

		var lens=get_lens_without_q();
		lens=lens.replace(/&fq=geo%3A%5B.+%5D/,"");
		url =  lens + geospatial() + get_sort() + "&q=" + encodeURIComponent(q);

		load_results(url);
	}
}

function reload_results() {
	load_results(window.location.href);
}

function reload_results_from_beginning() {
    url = window.location.href.replace(/&start=[0-9]+/, "");
    load_results(url);
}

function load_results(query) {
	query = query.replace("%20", "+"); // fix for #133
	History.pushStateWithoutReloadingResults(null, null, fixUrl(query));
	$("#results").fadeTo(0,0.3);
	$("#results").load_sync(query, {
		"v.template" : "ui/results"
	}, function() {
		process_results();
		$("#results").fadeTo(0,1);
	});
}

function load_main(query) {
	History.pushStateWithoutReloadingResults(null, null, fixUrl(query));
	$("#main").fadeTo(0,0.3);
	$("#main").load_sync(query + " #main", {
		"v.template" : "ui/results",
		"facet" : "off"
	}, function() {
		$("#main", this).unwrap();
		process_docs();
		pagination.process();
		process_filters();
		$("#main").fadeTo(0,1);
	});
}

function load_detail(doc) {
	var doi = $(".title a", doc).first().attr("href");
	doi=doi.replace("http://dx.doi.org/","");
	var detail = $(".full",doc);
	if (detail.text().length == 0) { // not already loaded
		detail.load("ui-detail", {
			q : 'doi:"' + doi + '"'
		}, function() {
			// hack to display xml. How to do with velocity?!
			$.get('api', { fl: "xml", wt: "csv", q: "doi:" + doi, rows: 1, "csv.header" :false},
					function(data) {
				var xml = $.base64Decode(data);
				if (xml.length != 0) {
					var pre = $("<pre>").text(xml);
					detail.append(pre);
				}
			});
		});
	};
}

function load_more_facet(query, facet_field) {
	var facet = $("#facet-" + facet_field);
	var div = $(".facet_data",facet);
    $(".more", div).append(throbber());

	div.load_sync(query + " .facet_data", {
		"v.template" : "ui/facet_fields",
		"facet.field" : facet_field,
		rows: 0
	}, function() {
		$(".facet_data", div).unwrap();
		process_facets();
	});
}

/******************
 * functions for processing content after loading
 ******************/

function process_results() {
	process_docs();
	process_facets();
	process_filters();
	process_oailink();
	pagination.process();
	setupsearchmap();
	if (options.get("continous"))
		makeFooterSticky();
	check_page_layout();
}

function process_apilinks() {                    
    var rows = $(".doc").length;
    $(".footer a.api").each(function() {
        var a = $(this);
        var href = a.attr("href");
        console.log("replace for", href);
        href = href.replace(/&rows=[0-9]+/, "&rows=" + rows);
        a.attr("href", href);
    });
}

function process_oailink() {
	var lens = $("#lens").attr("href");
	var oai_url = $("#oai").attr("href");
	oai_url += "&set=~" + $.base64urlEncode(lens);
	$("#oai").attr("href", oai_url);
}
	
function process_docs() {
	$(".doc a").unbind();
	$(".doc .count").unbind().click(function() {
		var doc = this.parentNode.parentNode;
		var detail = $(".full", doc);
		load_detail(doc);
		$(".short", doc).slideToggle();
		$(".full", doc).slideToggle();
		return false;
	});
	$(".doc .score a").click(function() {
		var score = this.parentNode;
		$(".exp", score).slideToggle();
		return false;
	});

	setupmap();
	setupabstract();
}

var facet_state = new Array();
function process_facets() {
	$(".facet").each(function() {
		var id = $(this).attr("id");
		var facet = id.replace(/facet-/,"");
		if (facet_state[facet] == undefined)
			facet_state[facet] = false;
		
		var data = $(".facet_data", this);
		$("h3",this).unbind().click(function() {
			facet_state[facet] = !facet_state[facet];
			data.slideToggle();
		});
		data.toggle(facet_state[facet]);
		
		$("a.more",this).unbind().click(function() {
			var query = $(this).attr("href"); 
			load_more_facet(query, facet);
			return false;
		});
		$("li", this).unbind().each(function() {
			var url = $("a",this).attr("href");
			var value = $("span.value",this).text();
			$(this).click(function() {
				load_results(url);
				$(window).scrollTop(0);
				return false;
			});
			$(this).hover(function() {
				filter_preview.show(url);
			}, function() {
				filter_preview.clear();
			});
		});
		var isNonEmpty = $("li:not(.hidden)",this).length != 0;
		$(this).toggle(isNonEmpty);
	});
	
	var hasVisibleElements = $(".facet:visible").length != 0;
	$("#facets").toggle(hasVisibleElements);
	
}

function process_filters() {
	$("#filters a").unbind().click(function() {
		url = $(this).attr("href");
		load_results(url);
		return false;
	});
}

/******************
 * Filter Preview
 ******************/

filter_preview = {
	timeout : null,
	show : function(query) {
		if (options.get("filter_preview")) {
			clearTimeout(this.timeout);
			var self = this;
			this.timeout = setTimeout(function() {
				self.do_preview(query);
			}, 250);
		};
	},
	clear : function() {
		clearTimeout(this.timeout);
		$(".doc").fadeTo(0,1);
	},
	do_preview : function (query) {
		var start = $("#start").text();
		var rows = $(".doc").length;
		$.ajax({
			type : "GET",
			url : "ui-ids" + query,
			data : {
				rows : start + rows
			},
			dataType : "json",
			cache: false,
			async: true,
			success: function(data) {
				var ids = data.response.docs.map(function(elem) {
					return elem.dataset_id;
				});
				$(".doc").each(function() {
					var id = $(this).attr("id");
					id = id.replace(/^result-/,"");
					if (ids.indexOf(id) == -1) {
						$(this).fadeTo(0,0.5);
					}
				});
			}
		});
	}
}

/******************
 * Layout: Welcome Page or Result List
 ******************/

check_page_layout = function() {
	is_welcome_page = window.location.search === "" && window.location.hash === "";
	$("#header *").toggleClass("homepage", is_welcome_page);
	$("#main").toggle(!is_welcome_page);
}

/******************
 * Options
 ******************/

var options = {
	opts : new Array(),
	init : function() {
		this.add("instant", false, null, "Instant Search", "load results immediately without clicking 'search' button");
		this.add("continous", true, reload_results_from_beginning, "Continous Scrolling", "load next results automatically when hitting the bottom of the page");
		this.add("filter_preview", false, null, "Filter Preview", "show preview of filter on mouse hover");
		options.menu.init();
	},
	add : function(name, defaultvalue, hook, text, tooltip) {
		var cookie = $.cookie(name)
		var value = cookie ? cookie == "true" : defaultvalue;
		this.opts[name] = {
				"value" : value,
				"hook" : hook
		};
		if (text == null)
			return;
		
		var a = $("<a href='#'>?</a>").click(function() { options.toggle(name); return false; });
		var span_name = $("<span>").html(text);
		if (tooltip)
			span_name.attr("title", tooltip);
		var span_option = $("<span>").attr("id", "option-" + name).addClass("option")
			.append(span_name, " is ", a);
		if ($(".option").length > 0)
			span_option.prepend( " | ");
		$("#options").append(span_option);
		this.refreshStatusText(name);
	},
	
	get : function(name) {
		return this.opts[name].value;
	},
	set : function(name, value) {
		this.opts[name].value = value;
		$.cookie(name, value, { expires: 365 });
		this.refreshStatusText(name);
		var hook = this.opts[name].hook;
		if (hook != null)
			hook();
	},
	toggle : function(name) {
		this.set(name, !this.get(name));
	},
	refreshStatusText : function(name) {
		var text = this.get(name)?"enabled":"disabled";
		$("#option-" + name + " a").html(text);
	}
}

options.menu = {
	init : function() {
		options.add("show_options", false, this.toggle);
		add_topbar_link("Options", function() { options.toggle("show_options") });
		$("#options").toggle(options.get("show_options"));
	},
	toggle : function() {
		$("#options").slideToggle();
	}
}


/******************
 * Pagination
 ******************/

var pagination = {
	init : function() {
		this.init_continous();
		this.process();
	},
	init_continous : function() {
		$(window).scroll(function(data) {
			if (pagination.next_page.is_needed())
				pagination.next_page.load(true);
		});
	},
	process : function() {
		$("#next_page_loading").hide();
		if (options.get("continous"))
			$(".pagination").hide();
		if (pagination.next_page.is_needed())
			pagination.next_page.load();
		$(".pagination a").unbind().click(function() {
			url = $(this).attr("href");
			load_main(url);
			return false;
		});
		$("#next_page a").unbind().click(function() {
			pagination.next_page.load();
			return false;
		});
	}
}

pagination.next_page = {
	loading : false,
	load : function(async) {
		if (async == null)
			async = false;

		if (this.loading)
			return
		this.loading = true;
		$("#next_page_loading").show();

		var sorting=get_sort().substr(6).replace("+"," ");

		$.ajax({
			type  : "GET",
			url : get_lens(),
			data :  {
				"v.template" : "ui/docs",
				facet: "off",
				sort: sorting,
				start : $(".doc").length
			}, 
			cache: false,
			async: async,
			success: function(data) {
				if (data.length == 0) {
					$("#next_page").hide();
				} else {
					$("#docs").append(data);
					process_docs();
	                process_apilinks();
				}
				$("#next_page_loading").hide();
				pagination.next_page.loading = false;

				if (pagination.next_page.is_needed())
					pagination.next_page.load(async);
			}
		});
	},
	is_needed : function () {
		if (options.get("continous") && $("#next_page").is(':visible')) { 
			var advance_in_pixel = 200;
			var div_top = $("#next_page").position().top;  
			var window_bottom = $(window).scrollTop() + $(window).height();
			return div_top - window_bottom < advance_in_pixel;
		} 
		return false;
	}
}

/******************
 * Startup
 ******************/

$(document).ready(init);
