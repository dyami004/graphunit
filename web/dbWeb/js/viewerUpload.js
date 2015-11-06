/**
 * This function makes a request to the setup servlet to display the demo page.
 * @returns {undefined}
 */


/*var datasets = [{name: "miserables", type: "json"}, {name: "imdb_small", type: "tsv"},
 {name: "imdb_large", type: "tsv"}, {name: "book_recommendation", type: "tsv"}]; */

var uploadedHtmls = [];
function uploadFiles() {
    alert("yay");
    var studyname = "Viewer";

    var thefiles = document.getElementById("thefiles").files;
    var url = "FileUpload?studyname=" + studyname;
    alert(url);
    alert(url);

    var formData = new FormData();
    for (var i = 0; i < thefiles.length; i++)
        formData.append("File", thefiles[i]);

    var xmlHttpRequest = getXMLHttpRequest();

    xmlHttpRequest.onreadystatechange = function()
    {

        if (xmlHttpRequest.readyState === 4 && xmlHttpRequest.status === 200)
        {

        }
    };

    xmlHttpRequest.open("POST", url, false);
    xmlHttpRequest.send(formData);
    alert("yay");

}

function getUploadedFileNames(thefiles) {
    alert("yay");
    var files = thefiles.files;
    //remove items in the uploadedHtml array;    
    while (uploadedHtmls.length > 0) {
        uploadedHtmls.pop();
    }

    for (var i = 0; i < files.length; i++) {
        //if the filename has an extension .html, .htm, xhtml, or .jsp we will add it to uploadedHtmls arrray       
        var name = files[i].name;
        if (name.indexOf(".html") > 0 || name.indexOf(".htm") > 0 || name.indexOf(".xhtml") > 0 || name.indexOf(".jsp") > 0) {
            uploadedHtmls.push(name);
        }
    }
}