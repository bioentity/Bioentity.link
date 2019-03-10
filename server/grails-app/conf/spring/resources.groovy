package spring
// Place your Spring DSL code here
beans = {
    multipartResolver(org.springframework.web.multipart.commons.CommonsMultipartResolver){
        maxInMemorySize=100000000
        maxUploadSize=100000000
        //uploadTempDir="/tmp"
    }
}
