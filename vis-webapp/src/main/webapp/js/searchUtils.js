/**
 * Created by chris on 11/19/15.
 */

// Appends the metadata to a search result.
function appendMetadata(result, docId, functions) {
    $.getJSON("metadata?docId=" + docId, function (data) {
        var titleBar = document.createElement("h2");
        titleBar.className = "resultHeader";
        var title = document.createElement("a");
        title.innerHTML = data.title;
        title.className = "res-title";
        title.href = "docs?docId=" + docId;
        title.target = "_blank";
        titleBar.appendChild(title);
        result.appendChild(titleBar);
        var author = document.createElement("p");
        author.innerHTML = data.author;
        author.className = "res-author";
        result.appendChild(author);
        var conference = document.createElement("p");
        conference.innerHTML = data.conference;
        conference.className = "res-conference";
        result.appendChild(conference);

        if(functions != null){
            functions.forEach(function(f){
                result.appendChild(document.createElement("br"));
                f(result, docId);
            });
        }

    });
}
