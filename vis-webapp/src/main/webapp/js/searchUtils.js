/**
 * Created by chris on 11/19/15.
 */

// Appends the metadata to a search result.
function appendMetadata(result, docId, functions) {
    $.getJSON("metadata?docId=" + docId, function (data) {
        var title = document.createElement("p");
        title.innerHTML = data.title;
        title.className = "res-title";
        result.appendChild(title);
        var author = document.createElement("p");
        author.innerHTML = data.author;
        author.className = "res-author";
        result.appendChild(author);
        var conference = document.createElement("p");
        conference.innerHTML = data.conference;
        conference.className = "res-conference";
        result.appendChild(conference);
        var link = document.createElement('a');
        link.innerHTML = "[LINK]";
        link.setAttribute("href", "docs?docId=" + docId);
        link.setAttribute("target", "_blank");
        result.appendChild(link);

        if(functions != null){
            functions.forEach(function(f){
                result.appendChild(document.createElement("br"));
                f(result, docId);
            });
        }

    });
}
