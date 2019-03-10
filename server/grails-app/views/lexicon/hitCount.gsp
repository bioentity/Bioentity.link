<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'markup.label', default: 'Markup')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-markup" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                             default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="list-markup" class="content scaffold-list" role="main">
<h1><g:message code="default.list.label" args="[entityName]"/></h1>
<g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
</g:if>
<table>
<tr>
    <th>Public Name</th>
    <th>Species</th>
    <th>Lexicon Class</th>
    <th># Pubs</th>
    <th>Markup Sizes</th>
</tr>
<g:each in="${markupList}" var="markup">
    <tr>
        <td>${markup.lookupTerm}</td>
    <td>${markup.species.name}</td>
    <td>${markup.className}</td>
    <td>${markup.publicationMap.size()}</td>
    <td>
    <g:each in="${markup.publicationMap}" var="pubMap">
        <button type="btn btn-info" >
        ${pubMap.key.title}
        <div class="badge badge-pill pill-info">${pubMap.value}</div>
        </div>
    </g:each>
    </td>
</tr>
</g:each>
</table>
%{--<f:table collection="${markupList}" />--}%

<div class="pagination">
    <g:paginate total="${markupCount ?: 0}"/>
</div>
</div>
</body>
</html>