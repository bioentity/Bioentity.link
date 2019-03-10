<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'lexicon.label', default: 'Lexicon')}" />
        <title>Lexicon ${lexicon.publicName}</title>
    </head>
    <body>
		<div class="row">
			<div class="col-md-3">
				<ul class="list-group">
					<li class="list-group-item">Public Name: ${lexicon.publicName}</li>
					<li class="list-group-item">Synonym: ${lexicon.synonym}</li>
					<li class="list-group-item">External ID: ${lexicon.externalModId}</li>
					<li class="list-group-item">Source: ${lexicon.lexiconSource?.source}</li>
					<li class="list-group-item">Class Name: ${lexicon.lexiconSource?.className}</li>
					<li class="list-group-item">Species: ${lexicon.lexiconSource?.species.name}</li>
				</ul>
			</div>
			<div class="col-md-9">
				<iframe src="${lexicon.link}" width="100%" height="1000px" frameborder="0"></iframe>
			</div>
		</div>
    </body>
</html>
