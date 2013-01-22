class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/rest"(parseRequest:true){
			controller = "top"
			action = [GET:"rest_list"]
		}
		"/rest/$controller"(parseRequest:true){
			action = [GET:"rest_list", POST:"rest_save"]
		}
		"/rest/$controller/$id"(parseRequest:true){
			action = [GET:"rest_show", PUT:"rest_update", DELETE:"rest_delete", POST:"rest_save"]
		} 

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
