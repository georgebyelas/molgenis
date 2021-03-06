<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['select2.css', 'bootstrap-datetimepicker.min.css', 'molgenis-form.css', 'usermanager.css']>
<#assign js=['jquery.validate.min.js', 'select2-patched.js', 'moment-with-locales.min.js', 'bootstrap-datetimepicker.min.js', "usermanager.js"]>

<@header css js/>
<div class="container-fluid">
	<div class="row" id="groupAndUserSwitches">
		<div class="col-md-12">
			<ul class="nav nav-pills" role="tablist">
				<li id="usersTab" <#if "users"==viewState>class="active"</#if>><a href="#user-manager" role="tab" data-toggle="tab">Users</a></li>
				<li id="groupsTab"<#if "groups"==viewState>class="active"</#if>><a href="#group-manager" role="tab" data-toggle="tab">Groups</a></li>
			</ul>
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-12">
			<div class="tab-content">
				<div class="tab-pane <#if "users"==viewState>active</#if>" id="user-manager">
					<legend>
						User management 
						<a id="create-user-btn" href="#" style="margin:30px 10px" data-toggle="modal" data-target="#managerModal">
							<img src="/img/new.png">
						</a>
					</legend>
				
					<table class="table">
						<thead>
							<tr>
								<th>Edit</th>
								<th>Active</th>
								<th>Username</th>
								<th>Full name</th>
								<#list groups as g><#if g.active><th>${g.name?html}</th></#if></#list>
							</tr>
						</thead>
				
						<tbody>
							<#if users?has_content>
								<#list users as user>
									<tr id="userRow${user.id?html}">
										<td><a href="#" class="edit-user-btn" data-toggle="modal" data-target="#managerModal" data-id="${user.id?html}"><img src="/img/editview.gif"></a></td>
										<td><input type="checkbox" class="activate-user-checkbox" data-id="${user.id?html}" <#if user.isActive()>checked</#if> <#if user.isSuperuser()>disabled</#if>></td>
										<td>${user.getUsername()!?html}</td>
										<td>${user.getFullName()!?html}</td>
										<#list groups as g><#if g.active><td><input type="checkbox" class="change-group-membership-checkbox" data-uid="${user.id?html}" data-gid="${g.id?html}"  <#if user.isGroupMember(g.id)>checked</#if>></td></#if></#list>
									</tr>
								</#list>
							</#if>
			 			</tbody>
					</table>
				</div>
			
				<div class="tab-pane <#if "groups"==viewState>active</#if>" id="group-manager">
					<legend>
						Group management 
						<a id="create-group-btn" href="#" style="margin:30px 10px" data-toggle="modal" data-target="#managerModal">
							<img src="/img/new.png">
						</a>
					</legend>
				
					<table class="table">
						<thead>
							<tr>
								<th>Edit</th>
								<th>Active</th>
								<th>Group name</th>
								<th>Usernames in group</th>
							</tr>
						</thead>
						
						<tbody>
							<#if groups?has_content>
								<#list groups as g>
									<tr id="groupRow${g.id?html}">
										<td>
											<a href="#" class="edit-group-btn" data-toggle="modal" data-target="#managerModal" data-id="${g.id?html}"><img src="/img/editview.gif"></a>
										</td>
										<td>
											<input type="checkbox" class="activate-group-checkbox" data-id="${g.id?html}" <#if g.active>checked</#if>>
										</td>
										<td>${g.getName()!?html}</td>
										<td>
											<#if users?has_content>
												<#assign setComma = false>
												<#list users as user>
													<#if user.isGroupMember(g.id)>
														<#if setComma>, </#if>
														${user.getUsername()!?html}
														<#assign setComma = true>
													</#if>
												</#list>
											</#if>
										</td>
									</tr>				
								</#list>
							</#if>
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
</div>

<#-- Modal -->
<div class="modal" id="managerModal" tabindex="-1" role="dialog" aria-labelledby="managerModalTitle" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">				
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        	<h4 class="modal-title" id="managerModalTitle"></h4>
	     	</div>
	      	<div class="modal-body">
				<div id="controlGroups"></div>
	      	</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        	<button type="button" id="submitFormButton" class="btn btn-primary">Save</button>
	      	</div>
	    </div>
	</div>
</div>

<@footer/>