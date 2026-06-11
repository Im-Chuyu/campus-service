<template>
  <div class="admin-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>Admin Console</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="House" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="House" @click="$router.back()">返回上一页</el-button>
        <el-button :icon="Refresh" @click="refreshCurrent">刷新</el-button>
      </div>
    </header>

    <main class="admin-main">
      <section class="hero">
        <div>
          <p class="eyebrow">Dashboard</p>
          <h2>管理员工作台</h2>
          <p>处理内容审核、分类、公告和举报，查看平台运营概况。</p>
        </div>
        <div class="hero-actions">
          <div class="auto-audit-card">
            <div>
              <strong>自动审核</strong>
              <span>{{ autoAuditConfig.canUpdate ? 'admin可修改' : '仅admin可修改' }}</span>
            </div>
            <el-switch
              v-model="autoAuditConfig.enabled"
              :loading="autoAuditLoading"
              :disabled="!autoAuditConfig.canUpdate || autoAuditLoading"
              @change="handleAutoAuditChange"
            />
          </div>
          <div class="admin-chip">
            <UserAvatar
              :size="55"
              :src="userStore.userInfo?.avatar"
              :text="avatarText"
              :role-id="userStore.userInfo?.roleId"
            />
            <div>
              <strong>{{ displayName }}</strong>
              <span>管理员</span>
            </div>
          </div>
        </div>
      </section>

      <section class="stat-grid" v-loading="statLoading">
        <div v-for="item in statCards" :key="item.label" class="stat-card">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </section>

      <section class="workspace">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">          <el-tab-pane label="内容管理" name="content">
            <div class="panel-head">
              <div>
                <h3>内容管理</h3>
                <p>查看全部内容并删除违规或过期文章</p>
              </div>
            </div>

            <div class="filter-line">
              <el-input v-model="contentQuery.title" placeholder="搜索标题" clearable @clear="reloadFirstPage('content')" @keyup.enter="reloadFirstPage('content')">
                <template #append>
                  <el-button :icon="Search" @click="reloadFirstPage('content')">搜索</el-button>
                </template>
              </el-input>
              <el-select v-model="contentQuery.status" placeholder="内容状态" clearable @change="reloadFirstPage('content')">
                <el-option label="待审核" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
                <el-option label="已下架" :value="3" />
              </el-select>
              <el-select v-model="contentQuery.categoryId" placeholder="分类" clearable @change="reloadFirstPage('content')">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <el-button :icon="Search" @click="reloadFirstPage('content')">查询</el-button>
            </div>

            <el-table :data="contentList" v-loading="contentLoading" empty-text="暂无内容">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="标题" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  <div class="title-with-pin">
                    <el-tag v-if="row.isTop === 1" size="small" type="warning">置顶</el-tag>
                    <span>{{ row.title }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="分类" width="120">
                <template #default="{ row }">{{ categoryMap[row.categoryId] || '未分类' }}</template>
              </el-table-column>
              <el-table-column label="发布者" width="100">
                <template #default="{ row }">用户{{ row.userId }}</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getContentStatusType(row.status)">{{ getContentStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="可见范围" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.isPrivate === 1 ? 'info' : 'success'">
                    {{ row.isPrivate === 1 ? '私密' : '公开' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="互动" width="160">
                <template #default="{ row }">
                  {{ row.viewCount || 0 }} 浏览 / {{ row.likeCount || 0 }} 赞
                </template>
              </el-table-column>
              <el-table-column label="发布时间" width="170">
                <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="goDetail(row.id)">查看</el-button>
                  <el-button size="small" type="warning" plain :loading="contentActionId === row.id" @click="handleToggleContentTop(row)">
                    {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
                  </el-button>
                  <el-button size="small" type="danger" plain :loading="contentActionId === row.id" @click="handleDeleteContent(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="admin-pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="contentPage.total"
              v-model:current-page="contentPage.page"
              v-model:page-size="contentPage.pageSize"
              :page-sizes="pageSizeOptions"
              @current-change="loadContents"
              @size-change="() => reloadFirstPage('content')"
            />
          </el-tab-pane>

          <el-tab-pane label="分类管理" name="category">
            <div class="panel-head">
              <div>
                <h3>分类管理</h3>
                <p>维护前台内容分类、排序和启用状态</p>
              </div>
              <el-button type="primary" :icon="Plus" @click="openCategoryDialog()">新增分类</el-button>
            </div>

            <el-table :data="categoryList" v-loading="categoryLoading" empty-text="暂无分类">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="分类名称" min-width="180" />
              <el-table-column prop="sort" label="排序" width="100" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="更新时间" width="170">
                <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="210" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="openCategoryDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain :loading="categoryActionId === row.id" @click="handleDeleteCategory(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="sub-panel-head">
              <div>
                <h3>活动通知子分类</h3>
                <p>用于在首页活动通知下按学院、组织或专题筛选活动</p>
              </div>
              <el-button type="primary" :icon="Plus" @click="openActivitySubCategoryDialog()">新增子分类</el-button>
            </div>

            <el-table :data="activitySubCategoryList" v-loading="activitySubCategoryLoading" empty-text="暂无活动通知子分类">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="子分类名称" min-width="180" />
              <el-table-column prop="sort" label="排序" width="100" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="更新时间" width="170">
                <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="210" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="openActivitySubCategoryDialog(row)">编辑</el-button>
                  <el-button size="small" type="danger" plain :loading="activitySubCategoryActionId === row.id" @click="handleDeleteActivitySubCategory(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="公告管理" name="notice">
            <div class="panel-head">
              <div>
                <h3>公告管理</h3>
                <p>发布公告、保存草稿并设置置顶；首页大卡片由独立配置控制</p>
              </div>
              <el-button type="primary" :icon="Plus" @click="openNoticeDialog()">新增公告</el-button>
            </div>

            <section class="hero-config-panel">
              <div class="hero-config-head">
                <div>
                  <h4>首页大卡片设置</h4>
                  <p>{{ isSuperAdmin ? '标题、内容和背景保存后会全站生效。' : '只有超级管理员admin可以修改。' }}</p>
                </div>
                <el-button type="primary" :loading="heroConfigSaving" :disabled="!isSuperAdmin" @click="handleSaveHeroConfig">
                  保存设置
                </el-button>
              </div>
              <el-form ref="heroConfigFormRef" :model="heroConfigForm" :rules="heroConfigRules" label-position="top">
                <div class="hero-config-grid">
                  <div>
                    <el-form-item label="大标题" prop="title">
                      <el-input v-model="heroConfigForm.title" maxlength="100" show-word-limit :disabled="!isSuperAdmin" />
                    </el-form-item>
                    <el-form-item label="正文内容" prop="content">
                      <el-input v-model="heroConfigForm.content" type="textarea" :rows="4" maxlength="300" show-word-limit :disabled="!isSuperAdmin" />
                    </el-form-item>
                  </div>
                  <el-form-item label="大卡片背景">
                    <input ref="noticeBackgroundInput" class="hidden-input" type="file" accept="image/*" @change="handleNoticeBackgroundChange" />
                    <div class="notice-bg-editor">
                      <div class="notice-bg-preview" :style="noticeBackgroundPreviewStyle">
                        <span>{{ heroConfigForm.background ? '自定义背景' : '跟随客户端主题' }}</span>
                      </div>
                      <div class="notice-bg-actions">
                        <el-button size="small" plain :disabled="!isSuperAdmin" @click="pickNoticeBackground">上传并裁剪</el-button>
                        <el-button size="small" plain :disabled="!isSuperAdmin" @click="resetNoticeBackground">恢复默认背景</el-button>
                      </div>
                    </div>
                  </el-form-item>
                </div>
              </el-form>
            </section>

            <div class="filter-line">
              <el-input v-model="noticeQuery.title" placeholder="搜索公告标题" clearable @clear="reloadFirstPage('notice')" @keyup.enter="reloadFirstPage('notice')">
                <template #append>
                  <el-button :icon="Search" @click="reloadFirstPage('notice')">搜索</el-button>
                </template>
              </el-input>
              <el-select v-model="noticeQuery.status" placeholder="状态" clearable @change="reloadFirstPage('notice')">
                <el-option label="已发布" :value="1" />
                <el-option label="草稿" :value="0" />
              </el-select>
              <el-button :icon="Search" @click="reloadFirstPage('notice')">查询</el-button>
            </div>

            <el-table :data="noticeList" v-loading="noticeLoading" empty-text="暂无公告">
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="置顶" width="90">
                <template #default="{ row }">
                  <el-tag v-if="row.isTop === 1" type="warning">置顶</el-tag>
                  <span v-else class="muted">否</span>
                </template>
              </el-table-column>
              <el-table-column label="更新时间" width="170">
                <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="270" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="openNoticeDialog(row)">编辑</el-button>
                  <el-button size="small" type="warning" plain @click="handleToggleNoticeTop(row)">
                    {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
                  </el-button>
                  <el-button size="small" type="danger" plain :loading="noticeActionId === row.id" @click="handleDeleteNotice(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="admin-pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="noticePage.total"
              v-model:current-page="noticePage.page"
              v-model:page-size="noticePage.pageSize"
              :page-sizes="pageSizeOptions"
              @current-change="loadNotices"
              @size-change="() => reloadFirstPage('notice')"
            />
          </el-tab-pane>

          <el-tab-pane label="举报处理" name="report">
            <div class="panel-head">
              <div>
                <h3>举报处理</h3>
                <p>查看用户提交的内容举报并记录处理结果</p>
              </div>
            </div>

            <div class="filter-line">
              <el-select v-model="reportQuery.status" placeholder="处理状态" clearable @change="reloadFirstPage('report')">
                <el-option label="待处理" :value="0" />
                <el-option label="已处理" :value="1" />
              </el-select>
              <el-input v-model="reportQuery.contentId" placeholder="内容 ID" clearable @clear="reloadFirstPage('report')" @keyup.enter="reloadFirstPage('report')">
                <template #append>
                  <el-button :icon="Search" @click="reloadFirstPage('report')">搜索</el-button>
                </template>
              </el-input>
              <el-button :icon="Search" @click="reloadFirstPage('report')">查询</el-button>
            </div>

            <el-table :data="reportList" v-loading="reportLoading" empty-text="暂无举报">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="contentId" label="内容ID" width="100" />
              <el-table-column prop="reportType" label="类型" width="130" />
              <el-table-column prop="reportReason" label="原因" min-width="220" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已处理' : '待处理' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="提交时间" width="170">
                <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="goDetail(row.contentId)">查看内容</el-button>
                  <el-button size="small" type="primary" plain :disabled="row.status === 1" @click="openReportDialog(row)">处理</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="admin-pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="reportPage.total"
              v-model:current-page="reportPage.page"
              v-model:page-size="reportPage.pageSize"
              :page-sizes="pageSizeOptions"
              @current-change="loadReports"
              @size-change="() => reloadFirstPage('report')"
            />
          </el-tab-pane>

          <el-tab-pane label="消息管理" name="message">
            <div class="panel-head">
              <div>
                <h3>消息管理</h3>
                <p>查看站内消息，并向指定用户发送通知</p>
              </div>
              <el-button type="primary" :icon="Plus" @click="openMessageDialog">发送消息</el-button>
            </div>

            <div class="filter-line">
              <el-select v-model="messageQuery.type" placeholder="消息类型" @change="reloadFirstPage('message')">
                <el-option label="全部类型" value="" />
                <el-option label="审核通知" value="AUDIT" />
                <el-option label="公告通知" value="NOTICE" />
                <el-option label="举报通知" value="REPORT" />
                <el-option label="系统消息" value="SYSTEM" />
              </el-select>
              <el-select v-model="messageQuery.isRead" placeholder="阅读状态" @change="reloadFirstPage('message')">
                <el-option label="全部状态" :value="null" />
                <el-option label="未读" :value="0" />
                <el-option label="已读" :value="1" />
              </el-select>
              <el-button :icon="Search" @click="reloadFirstPage('message')">查询</el-button>
            </div>

            <el-table :data="adminMessageList" v-loading="messageLoading" empty-text="暂无消息">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="userId" label="接收用户" width="110" />
              <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
              <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
              <el-table-column label="类型" width="110">
                <template #default="{ row }">{{ getMessageTypeText(row.type) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.isRead === 0 ? 'warning' : 'info'">{{ row.isRead === 0 ? '未读' : '已读' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="发送时间" width="170">
                <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="admin-pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="messagePage.total"
              v-model:current-page="messagePage.page"
              v-model:page-size="messagePage.pageSize"
              :page-sizes="pageSizeOptions"
              @current-change="loadAdminMessages"
              @size-change="() => reloadFirstPage('message')"
            />
          </el-tab-pane>

          <el-tab-pane label="用户管理" name="user">
            <div class="panel-head">
              <div>
                <h3>用户管理</h3>
                <p>查看账号状态，禁用违规用户或恢复账号</p>
              </div>
            </div>

            <div class="filter-line">
              <el-input v-model="userQuery.keyword" placeholder="搜索用户名、昵称、手机号或邮箱" clearable @clear="reloadFirstPage('user')" @keyup.enter="reloadFirstPage('user')">
                <template #append>
                  <el-button :icon="Search" @click="reloadFirstPage('user')">搜索</el-button>
                </template>
              </el-input>
              <el-select v-model="userQuery.roleId" placeholder="角色" clearable @change="reloadFirstPage('user')">
                <el-option label="管理员" :value="1" />
                <el-option label="普通用户" :value="2" />
              </el-select>
              <el-select v-model="userQuery.status" placeholder="账号状态" clearable @change="reloadFirstPage('user')">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <el-button :icon="Search" @click="reloadFirstPage('user')">查询</el-button>
            </div>

            <el-table :data="userList" v-loading="userLoading" empty-text="暂无用户">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column label="用户名" min-width="130">
                <template #default="{ row }">
                  <el-button class="user-link" link type="primary" @click="goUserProfile(row.id)">
                    {{ row.username }}
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="nickname" label="昵称" min-width="130" />
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
              <el-table-column label="角色" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.roleId === 1 ? 'danger' : 'info'">{{ row.roleId === 1 ? '管理员' : '普通用户' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="170">
                <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    :type="row.status === 1 ? 'danger' : 'success'"
                    plain
                    :disabled="row.id === userStore.userInfo?.id || (row.roleId === 1 && !isSuperAdmin)"
                    :loading="userActionId === row.id"
                    @click="handleToggleUserStatus(row)"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    :disabled="!canDeleteUser(row)"
                    :loading="userActionId === row.id"
                    @click="handleDeleteUser(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="admin-pagination"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="userPage.total"
              v-model:current-page="userPage.page"
              v-model:page-size="userPage.pageSize"
              :page-sizes="pageSizeOptions"
              @current-change="loadUsers"
              @size-change="() => reloadFirstPage('user')"
            />
          </el-tab-pane>
        </el-tabs>
      </section>
    </main>

    <el-dialog v-model="rejectDialogVisible" title="拒绝内容" width="460px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" maxlength="255" show-word-limit placeholder="拒绝原因可选，留空即可" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="auditActionId === currentAudit?.id" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" class="full-width" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio-button :value="1">启用</el-radio-button>
            <el-radio-button :value="0">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySaving" @click="handleSaveCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="activitySubCategoryDialogVisible" :title="activitySubCategoryForm.id ? '编辑活动通知子分类' : '新增活动通知子分类'" width="460px">
      <el-form ref="activitySubCategoryFormRef" :model="activitySubCategoryForm" :rules="activitySubCategoryRules" label-position="top">
        <el-form-item label="子分类名称" prop="name">
          <el-input v-model="activitySubCategoryForm.name" maxlength="50" show-word-limit placeholder="例如：理学院、大数据工程学院" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="activitySubCategoryForm.sort" :min="0" class="full-width" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="activitySubCategoryForm.status">
            <el-radio-button :value="1">启用</el-radio-button>
            <el-radio-button :value="0">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activitySubCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="activitySubCategorySaving" @click="handleSaveActivitySubCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeDialogVisible" :title="noticeForm.id ? '编辑公告' : '新增公告'" width="620px">
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="noticeRules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" maxlength="100" show-word-limit placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="noticeForm.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="noticeForm.status">
              <el-radio-button :value="1">发布</el-radio-button>
              <el-radio-button :value="0">草稿</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="置顶" prop="isTop">
            <el-switch v-model="noticeForm.isTop" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noticeSaving" @click="handleSaveNotice">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeCropDialogVisible" title="裁剪首页大卡片背景" width="760px" @closed="resetNoticeCropper">
      <div class="notice-cropper">
        <div class="notice-crop-stage" @mousedown="startNoticeCropDrag" @mousemove="onNoticeCropDrag" @mouseup="stopNoticeCropDrag" @mouseleave="stopNoticeCropDrag">
          <img
            v-if="noticeCropImageUrl"
            class="notice-crop-image"
            :src="noticeCropImageUrl"
            :style="noticeCropImageStyle"
            draggable="false"
          />
          <div class="notice-crop-mask"></div>
          <div class="notice-crop-frame"></div>
        </div>
        <div class="notice-crop-controls">
          <span>缩放</span>
          <el-slider v-model="noticeCropScale" :min="noticeCropMinScale" :max="4" :step="0.01" />
        </div>
      </div>
      <template #footer>
        <el-button @click="noticeCropDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmNoticeCrop">确认使用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="处理举报" width="520px">
      <div v-if="currentReport" class="report-summary">
        <strong>#{{ currentReport.id }} 内容 {{ currentReport.contentId }}</strong>
        <span>{{ currentReport.reportType }}：{{ currentReport.reportReason }}</span>
      </div>
      <el-input v-model="reportHandleResult" type="textarea" :rows="4" maxlength="255" show-word-limit placeholder="请输入处理结果" />
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reportSaving" @click="handleSaveReport">保存处理结果</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="messageDialogVisible" title="发送消息" width="560px">
      <el-alert
        class="dialog-tip"
        title="这个功能用于给指定用户发送站内通知。接收用户 ID 可在用户管理表格第一列查看；关联业务 ID 可不填，只有需要点击消息跳转到某条内容时才填写内容 ID。"
        type="info"
        :closable="false"
      />
      <el-form ref="messageFormRef" :model="messageForm" :rules="messageRules" label-position="top">
        <el-form-item label="接收用户 ID" prop="userId">
          <el-input-number v-model="messageForm.userId" :min="1" class="full-width" placeholder="填写用户管理里的用户 ID" />
        </el-form-item>
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="messageForm.type" class="full-width" placeholder="请选择消息类型">
            <el-option label="审核通知" value="AUDIT" />
            <el-option label="公告通知" value="NOTICE" />
            <el-option label="举报通知" value="REPORT" />
            <el-option label="系统消息" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="messageForm.title" maxlength="100" show-word-limit placeholder="例如：资料补充提醒" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="messageForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="写给用户看的通知正文，会出现在用户的消息中心。" />
        </el-form-item>
        <el-form-item label="关联内容 ID（可选）">
          <el-input-number v-model="messageForm.relatedId" :min="1" class="full-width" placeholder="不需要跳转时留空" />
          <p class="field-help">填写内容 ID 后，用户可从消息跳到对应帖子；普通系统通知可以不填。</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="messageSaving" @click="handleSendMessage">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Collection,
  DocumentChecked,
  Files,
  House,
  Plus,
  Refresh,
  Search,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getDashboardStatApi } from '../api/statistics'
  import { getWaitAuditPageApi, handleAuditApi } from '../api/audit'
  import { addCategoryApi, deleteCategoryApi, getCategoryListApi, updateCategoryApi } from '../api/category'
  import { addNoticeApi, deleteNoticeApi, getNoticePageApi, toggleNoticeTopApi, updateNoticeApi } from '../api/notice'
  import { getReportPageApi, handleReportApi } from '../api/report'
  import { deleteContentApi, getContentPageApi, toggleContentTopApi } from '../api/content'
  import { deleteUserApi, getUserPageApi, updateUserStatusApi } from '../api/user'
  import { getMessagePageApi, sendMessageApi } from '../api/message'
import {
  addActivitySubCategoryApi,
  deleteActivitySubCategoryApi,
  getAdminActivitySubCategoryListApi,
  updateActivitySubCategoryApi
} from '../api/activitySubCategory'
import { uploadImageApi } from '../api/upload'
import { getAutoAuditConfigApi, updateAutoAuditConfigApi } from '../api/autoAudit'
import { getHomeHeroConfigApi, updateHomeHeroConfigApi } from '../api/homeHero'
import { formatBeijingDateTime } from '../utils/time'
import UserAvatar from '../components/UserAvatar.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('content')
const pageSizeOptions = [10, 20, 50, 100]
const statLoading = ref(false)
const stats = ref({})
const autoAuditLoading = ref(false)
const autoAuditConfig = reactive({ enabled: true, canUpdate: false })

const auditLoading = ref(false)
const auditActionId = ref(null)
const auditList = ref([])
const auditPage = reactive({ page: 1, pageSize: 10, total: 0 })
const currentAudit = ref(null)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')

const contentLoading = ref(false)
const contentActionId = ref(null)
const contentList = ref([])
const contentQuery = reactive({ title: '', status: null, categoryId: null })
const contentPage = reactive({ page: 1, pageSize: 10, total: 0 })

const categoryLoading = ref(false)
const categorySaving = ref(false)
const categoryActionId = ref(null)
const categoryDialogVisible = ref(false)
const categoryFormRef = ref()
const categoryList = ref([])
const categoryForm = reactive({ id: null, name: '', sort: 0, status: 1 })
const activitySubCategoryLoading = ref(false)
const activitySubCategorySaving = ref(false)
const activitySubCategoryActionId = ref(null)
const activitySubCategoryDialogVisible = ref(false)
const activitySubCategoryFormRef = ref()
const activitySubCategoryList = ref([])
const activitySubCategoryForm = reactive({ id: null, name: '', sort: 0, status: 1 })

const noticeLoading = ref(false)
const noticeSaving = ref(false)
const noticeActionId = ref(null)
const noticeDialogVisible = ref(false)
const noticeCropDialogVisible = ref(false)
const heroConfigSaving = ref(false)
const heroConfigFormRef = ref()
const noticeFormRef = ref()
const noticeBackgroundInput = ref()
const noticeList = ref([])
const noticeQuery = reactive({ title: '', status: null })
const noticePage = reactive({ page: 1, pageSize: 10, total: 0 })
const noticeForm = reactive({ id: null, title: '', content: '', heroBackground: '', status: 1, isTop: 0 })
const heroConfigForm = reactive({
  title: '',
  content: '',
  background: '',
  canUpdate: false
})
const noticeBackgroundFile = ref(null)
const noticeCropImageUrl = ref('')
const noticeCropImageElement = ref(null)
const noticeCropScale = ref(1)
const noticeCropMinScale = ref(1)
const noticeCropOffset = reactive({ x: 0, y: 0 })
const noticeCropDrag = reactive({ active: false, x: 0, y: 0, startX: 0, startY: 0 })
const NOTICE_BG_WIDTH = 1600
const NOTICE_BG_HEIGHT = 476
const NOTICE_CROP_FRAME = { width: 560, height: 167 }

const reportLoading = ref(false)
const reportSaving = ref(false)
const reportDialogVisible = ref(false)
const reportList = ref([])
const reportQuery = reactive({ status: 0, contentId: '' })
const reportPage = reactive({ page: 1, pageSize: 10, total: 0 })
const currentReport = ref(null)
const reportHandleResult = ref('')

const userLoading = ref(false)
const userActionId = ref(null)
const userList = ref([])
const userQuery = reactive({ keyword: '', status: null, roleId: null })
const userPage = reactive({ page: 1, pageSize: 10, total: 0 })

const messageLoading = ref(false)
const messageSaving = ref(false)
const messageDialogVisible = ref(false)
const messageFormRef = ref()
const adminMessageList = ref([])
const messageQuery = reactive({ type: '', isRead: null })
const messagePage = reactive({ page: 1, pageSize: 10, total: 0 })
const messageForm = reactive({
  userId: null,
  title: '',
  content: '',
  type: 'SYSTEM',
  relatedId: null
})

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const activitySubCategoryRules = {
  name: [{ required: true, message: '请输入活动通知子分类名称', trigger: 'blur' }]
}

const noticeRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const heroConfigRules = {
  title: [{ required: true, message: '请输入首页大卡片标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入首页大卡片内容', trigger: 'blur' }]
}

const messageRules = {
  userId: [{ required: true, message: '请输入接收用户 ID', trigger: 'blur' }],
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择消息类型', trigger: 'change' }]
}

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')
const avatarText = computed(() => displayName.value.slice(0, 1))
const isSuperAdmin = computed(() => userStore.isSuperAdmin)
const canDeleteUser = row => {
  if (!row || row.id === userStore.userInfo?.id || row.username === 'admin') {
    return false
  }
  return row.roleId !== 1 || isSuperAdmin.value
}
const noticeBackgroundPreviewStyle = computed(() => (
  heroConfigForm.background ? { backgroundImage: `url(${heroConfigForm.background})` } : {}
))
const noticeCropImageStyle = computed(() => ({
  width: `${(noticeCropImageElement.value?.naturalWidth || 0) * noticeCropScale.value}px`,
  height: `${(noticeCropImageElement.value?.naturalHeight || 0) * noticeCropScale.value}px`,
  transform: `translate(calc(-50% + ${noticeCropOffset.x}px), calc(-50% + ${noticeCropOffset.y}px))`
}))
const categoryMap = computed(() => {
  const map = {}
  categoryList.value.forEach(item => {
    map[item.id] = item.name
  })
  return map
})

const statCards = computed(() => [
  { label: '用户总数', value: stats.value.userTotal || 0, icon: User },
  { label: '内容总数', value: stats.value.contentTotal || 0, icon: Files },
  { label: '分类数量', value: stats.value.categoryTotal || 0, icon: Collection },
  { label: '今日新增', value: stats.value.todayContentTotal || 0, icon: TrendCharts },
  { label: '已审核内容', value: stats.value.auditedContentTotal || 0, icon: DocumentChecked },
  { label: '审核通过率', value: `${stats.value.auditPassRate || 0}%`, icon: TrendCharts }
])

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const fileToDataUrl = file =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = reject
    reader.readAsDataURL(file)
  })

const loadImage = src =>
  new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = reject
    image.src = src
  })

const canvasToBlob = canvas =>
  new Promise(resolve => {
    canvas.toBlob(blob => resolve(blob), 'image/jpeg', 0.86)
  })

const clampNoticeCropOffset = () => {
  if (!noticeCropImageElement.value) return
  const imageWidth = noticeCropImageElement.value.naturalWidth * noticeCropScale.value
  const imageHeight = noticeCropImageElement.value.naturalHeight * noticeCropScale.value
  const maxX = Math.max(0, (imageWidth - NOTICE_CROP_FRAME.width) / 2)
  const maxY = Math.max(0, (imageHeight - NOTICE_CROP_FRAME.height) / 2)
  noticeCropOffset.x = Math.min(maxX, Math.max(-maxX, noticeCropOffset.x))
  noticeCropOffset.y = Math.min(maxY, Math.max(-maxY, noticeCropOffset.y))
}

const pickNoticeBackground = () => noticeBackgroundInput.value?.click()

const resetNoticeBackground = () => {
  heroConfigForm.background = ''
  noticeBackgroundFile.value = null
}

const handleNoticeBackgroundChange = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  noticeCropImageUrl.value = await fileToDataUrl(file)
  noticeCropImageElement.value = await loadImage(noticeCropImageUrl.value)
  const minScaleX = NOTICE_CROP_FRAME.width / noticeCropImageElement.value.naturalWidth
  const minScaleY = NOTICE_CROP_FRAME.height / noticeCropImageElement.value.naturalHeight
  noticeCropMinScale.value = Math.max(minScaleX, minScaleY, 0.1)
  noticeCropScale.value = noticeCropMinScale.value
  noticeCropOffset.x = 0
  noticeCropOffset.y = 0
  noticeCropDialogVisible.value = true
  event.target.value = ''
}

const startNoticeCropDrag = event => {
  noticeCropDrag.active = true
  noticeCropDrag.x = event.clientX
  noticeCropDrag.y = event.clientY
  noticeCropDrag.startX = noticeCropOffset.x
  noticeCropDrag.startY = noticeCropOffset.y
}

const onNoticeCropDrag = event => {
  if (!noticeCropDrag.active) return
  noticeCropOffset.x = noticeCropDrag.startX + event.clientX - noticeCropDrag.x
  noticeCropOffset.y = noticeCropDrag.startY + event.clientY - noticeCropDrag.y
  clampNoticeCropOffset()
}

const stopNoticeCropDrag = () => {
  noticeCropDrag.active = false
}

watch(noticeCropScale, clampNoticeCropOffset)

const confirmNoticeCrop = async () => {
  if (!noticeCropImageElement.value) return
  const canvas = document.createElement('canvas')
  canvas.width = NOTICE_BG_WIDTH
  canvas.height = NOTICE_BG_HEIGHT
  const ctx = canvas.getContext('2d')
  const sourceWidth = NOTICE_CROP_FRAME.width / noticeCropScale.value
  const sourceHeight = NOTICE_CROP_FRAME.height / noticeCropScale.value
  const sourceX = (noticeCropImageElement.value.naturalWidth - sourceWidth) / 2 - noticeCropOffset.x / noticeCropScale.value
  const sourceY = (noticeCropImageElement.value.naturalHeight - sourceHeight) / 2 - noticeCropOffset.y / noticeCropScale.value
  ctx.drawImage(noticeCropImageElement.value, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, NOTICE_BG_WIDTH, NOTICE_BG_HEIGHT)
  const blob = await canvasToBlob(canvas)
  heroConfigForm.background = canvas.toDataURL('image/jpeg', 0.86)
  noticeBackgroundFile.value = blob ? new File([blob], 'notice-hero.jpg', { type: 'image/jpeg' }) : null
  noticeCropDialogVisible.value = false
}

const resetNoticeCropper = () => {
  noticeCropImageUrl.value = ''
  noticeCropImageElement.value = null
  noticeCropScale.value = 1
  noticeCropMinScale.value = 1
  noticeCropOffset.x = 0
  noticeCropOffset.y = 0
}

const getContentStatusText = status => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已下架'
  }
  return map[status] || '未知'
}

const getContentStatusType = status => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || 'info'
}

const getMessageTypeText = type => {
  const map = {
    AUDIT: '审核通知',
    NOTICE: '公告通知',
    REPORT: '举报通知',
    SYSTEM: '系统消息'
  }
  return map[type] || type || '消息'
}

const loadStats = async () => {
  statLoading.value = true
  try {
    const res = await getDashboardStatApi()
    stats.value = res.data || {}
  } finally {
    statLoading.value = false
  }
}

const loadAutoAuditConfig = async () => {
  autoAuditLoading.value = true
  try {
    const res = await getAutoAuditConfigApi()
    autoAuditConfig.enabled = res.data?.enabled ?? true
    autoAuditConfig.canUpdate = res.data?.canUpdate ?? false
  } finally {
    autoAuditLoading.value = false
  }
}

const loadAuditList = async () => {
  auditLoading.value = true
  try {
    const res = await getWaitAuditPageApi({
      page: auditPage.page,
      pageSize: auditPage.pageSize
    })
    const data = res.data || {}
    auditList.value = data.records || []
    auditPage.total = Number(data.total || 0)
  } finally {
    auditLoading.value = false
  }
}

const loadContents = async () => {
  contentLoading.value = true
  try {
    const params = {
      title: contentQuery.title || undefined,
      status: contentQuery.status ?? undefined,
      categoryId: contentQuery.categoryId || undefined,
      page: contentPage.page,
      pageSize: contentPage.pageSize
    }
    const res = await getContentPageApi(params)
    const data = res.data || {}
    contentList.value = data.records || []
    contentPage.total = Number(data.total || 0)
  } finally {
    contentLoading.value = false
  }
}

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getCategoryListApi()
    categoryList.value = res.data || []
  } finally {
    categoryLoading.value = false
  }
}

const loadActivitySubCategories = async () => {
  activitySubCategoryLoading.value = true
  try {
    const res = await getAdminActivitySubCategoryListApi()
    activitySubCategoryList.value = res.data || []
  } finally {
    activitySubCategoryLoading.value = false
  }
}

const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const params = {
      title: noticeQuery.title || undefined,
      status: noticeQuery.status ?? undefined,
      page: noticePage.page,
      pageSize: noticePage.pageSize
    }
    const res = await getNoticePageApi(params)
    const data = res.data || {}
    noticeList.value = data.records || []
    noticePage.total = Number(data.total || 0)
  } finally {
    noticeLoading.value = false
  }
}

const loadHeroConfig = async () => {
  const res = await getHomeHeroConfigApi()
  const data = res.data || {}
  heroConfigForm.title = data.title || '今天的校园动态，都从这里开始'
  heroConfigForm.content = data.content || '发布校园信息、查看活动通知、寻找失物线索，让校园服务更集中也更高效。'
  heroConfigForm.background = data.background || ''
  heroConfigForm.canUpdate = data.canUpdate ?? false
  noticeBackgroundFile.value = null
}

const loadReports = async () => {
  reportLoading.value = true
  try {
    const params = {
      status: reportQuery.status ?? undefined,
      contentId: reportQuery.contentId || undefined,
      page: reportPage.page,
      pageSize: reportPage.pageSize
    }
    const res = await getReportPageApi(params)
    const data = res.data || {}
    reportList.value = data.records || []
    reportPage.total = Number(data.total || 0)
  } finally {
    reportLoading.value = false
  }
}

const loadUsers = async () => {
  userLoading.value = true
  try {
    const params = {
      keyword: userQuery.keyword || undefined,
      status: userQuery.status ?? undefined,
      roleId: userQuery.roleId || undefined,
      page: userPage.page,
      pageSize: userPage.pageSize
    }
    const res = await getUserPageApi(params)
    const data = res.data || {}
    userList.value = data.records || []
    userPage.total = Number(data.total || 0)
  } finally {
    userLoading.value = false
  }
}

const loadAdminMessages = async () => {
  messageLoading.value = true
  try {
    const params = {
      type: messageQuery.type || undefined,
      isRead: messageQuery.isRead ?? undefined,
      page: messagePage.page,
      pageSize: messagePage.pageSize
    }
    const res = await getMessagePageApi(params)
    const data = res.data || {}
    adminMessageList.value = data.records || []
    messagePage.total = Number(data.total || 0)
  } finally {
    messageLoading.value = false
  }
}

const reloadFirstPage = type => {
  const pageMap = {
    audit: auditPage,
    content: contentPage,
    notice: noticePage,
    report: reportPage,
    message: messagePage,
    user: userPage
  }
  if (pageMap[type]) {
    pageMap[type].page = 1
  }
  handleTabChange(type)
}

const handleTabChange = name => {
  if (name === 'audit') loadAuditList()
  if (name === 'content') loadContents()
  if (name === 'category') {
    loadCategories()
    loadActivitySubCategories()
  }
  if (name === 'notice') {
    loadNotices()
    loadHeroConfig()
  }
  if (name === 'report') loadReports()
  if (name === 'message') loadAdminMessages()
  if (name === 'user') loadUsers()
}

const refreshCurrent = async () => {
  await Promise.all([loadStats(), loadAutoAuditConfig()])
  handleTabChange(activeTab.value)
}

const goDetail = id => {
  router.push(`/content/${id}`)
}

const goUserProfile = id => {
  router.push(`/profile/${id}`)
}

const handleApprove = async row => {
  auditActionId.value = row.id
  try {
    await handleAuditApi({ contentId: row.id, auditResult: 1, auditReason: '' })
    ElMessage.success('审核已通过')
    await Promise.all([loadAuditList(), loadStats()])
  } finally {
    auditActionId.value = null
  }
}

const openReject = row => {
  currentAudit.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  const reason = rejectReason.value.trim()
  if (!reason) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  auditActionId.value = currentAudit.value.id
  try {
    await handleAuditApi({
      contentId: currentAudit.value.id,
      auditResult: 2,
      auditReason: reason
    })
    ElMessage.success('已拒绝该内容')
    rejectDialogVisible.value = false
    await Promise.all([loadAuditList(), loadStats()])
  } finally {
    auditActionId.value = null
  }
}

const handleAutoAuditChange = async value => {
  autoAuditLoading.value = true
  try {
    await updateAutoAuditConfigApi(value)
    autoAuditConfig.enabled = value
    ElMessage.success(value ? '自动审核已开启' : '自动审核已关闭')
  } catch (error) {
    autoAuditConfig.enabled = !value
    throw error
  } finally {
    autoAuditLoading.value = false
  }
}

const handleDeleteContent = async row => {
  await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？删除后不可恢复。`, '删除文章', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  })

  contentActionId.value = row.id
  try {
    await deleteContentApi(row.id)
    ElMessage.success('文章已删除')
    await Promise.all([loadContents(), loadStats()])
    if (activeTab.value === 'audit') await loadAuditList()
  } finally {
    contentActionId.value = null
  }
}

const handleToggleContentTop = async row => {
  contentActionId.value = row.id
  try {
    await toggleContentTopApi(row.id)
    ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已置顶')
    await loadContents()
  } finally {
    contentActionId.value = null
  }
}

const openCategoryDialog = row => {
  categoryForm.id = row?.id || null
  categoryForm.name = row?.name || ''
  categoryForm.sort = row?.sort ?? 0
  categoryForm.status = row?.status ?? 1
  categoryDialogVisible.value = true
}

const handleSaveCategory = async () => {
  await categoryFormRef.value.validate()
  categorySaving.value = true
  try {
    const data = { ...categoryForm }
    if (data.id) {
      await updateCategoryApi(data)
    } else {
      await addCategoryApi(data)
    }
    ElMessage.success('分类已保存')
    categoryDialogVisible.value = false
    await Promise.all([loadCategories(), loadStats()])
  } finally {
    categorySaving.value = false
  }
}

const handleDeleteCategory = async row => {
  await ElMessageBox.confirm(
    `确定删除分类「${row.name}」吗？如果该分类下已有文章，系统会阻止删除。`,
    '删除分类',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  )
  categoryActionId.value = row.id
  try {
    await deleteCategoryApi(row.id)
    ElMessage.success('分类已删除')
    await Promise.all([loadCategories(), loadStats()])
  } catch (error) {
    const message = error?.message || error?.msg || error?.response?.data?.message || error?.response?.data?.msg
    if (message) {
      ElMessage.warning(message)
    }
  } finally {
    categoryActionId.value = null
  }
}

const openActivitySubCategoryDialog = row => {
  activitySubCategoryForm.id = row?.id || null
  activitySubCategoryForm.name = row?.name || ''
  activitySubCategoryForm.sort = row?.sort ?? 0
  activitySubCategoryForm.status = row?.status ?? 1
  activitySubCategoryDialogVisible.value = true
}

const handleSaveActivitySubCategory = async () => {
  await activitySubCategoryFormRef.value.validate()
  activitySubCategorySaving.value = true
  try {
    const data = { ...activitySubCategoryForm }
    if (data.id) {
      await updateActivitySubCategoryApi(data)
    } else {
      await addActivitySubCategoryApi(data)
    }
    ElMessage.success('活动通知子分类已保存')
    activitySubCategoryDialogVisible.value = false
    await loadActivitySubCategories()
  } finally {
    activitySubCategorySaving.value = false
  }
}

const handleDeleteActivitySubCategory = async row => {
  await ElMessageBox.confirm(
    `确定删除活动通知子分类「${row.name}」吗？如果该子分类下已有文章，系统会阻止删除。`,
    '删除活动通知子分类',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  )
  activitySubCategoryActionId.value = row.id
  try {
    await deleteActivitySubCategoryApi(row.id)
    ElMessage.success('活动通知子分类已删除')
    await loadActivitySubCategories()
  } catch (error) {
    const message = error?.message || error?.msg || error?.response?.data?.message || error?.response?.data?.msg
    if (message) {
      ElMessage.warning(message)
    }
  } finally {
    activitySubCategoryActionId.value = null
  }
}

const openNoticeDialog = row => {
  noticeForm.id = row?.id || null
  noticeForm.title = row?.title || ''
  noticeForm.content = row?.content || ''
  noticeForm.heroBackground = row?.heroBackground || ''
  noticeForm.status = row?.status ?? 1
  noticeForm.isTop = row?.isTop ?? 0
  noticeDialogVisible.value = true
}

const handleSaveHeroConfig = async () => {
  if (!isSuperAdmin.value) {
    ElMessage.warning('只有超级管理员admin可以修改首页大卡片')
    return
  }

  await heroConfigFormRef.value.validate()
  heroConfigSaving.value = true
  try {
    const data = {
      title: heroConfigForm.title,
      content: heroConfigForm.content,
      background: heroConfigForm.background
    }
    if (noticeBackgroundFile.value) {
      const res = await uploadImageApi(noticeBackgroundFile.value)
      data.background = res.data
      heroConfigForm.background = res.data
      noticeBackgroundFile.value = null
    }
    await updateHomeHeroConfigApi(data)
    ElMessage.success('首页大卡片已保存')
    await loadHeroConfig()
  } finally {
    heroConfigSaving.value = false
  }
}

const handleSaveNotice = async () => {
  await noticeFormRef.value.validate()
  noticeSaving.value = true
  try {
    const data = { ...noticeForm }
    data.heroBackground = ''
    if (data.id) {
      await updateNoticeApi(data)
    } else {
      await addNoticeApi(data)
    }
    ElMessage.success('公告已保存')
    noticeDialogVisible.value = false
    await loadNotices()
  } finally {
    noticeSaving.value = false
  }
}

const handleToggleNoticeTop = async row => {
  await toggleNoticeTopApi(row.id)
  ElMessage.success('置顶状态已更新')
  await loadNotices()
}

const handleDeleteNotice = async row => {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '删除公告', { type: 'warning' })
  noticeActionId.value = row.id
  try {
    await deleteNoticeApi(row.id)
    ElMessage.success('公告已删除')
    await loadNotices()
  } finally {
    noticeActionId.value = null
  }
}

const openReportDialog = row => {
  currentReport.value = row
  reportHandleResult.value = row.handleResult || ''
  reportDialogVisible.value = true
}

const handleSaveReport = async () => {
  if (!reportHandleResult.value.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  reportSaving.value = true
  try {
    await handleReportApi({
      reportId: currentReport.value.id,
      handleResult: reportHandleResult.value
    })
    ElMessage.success('举报已处理')
    reportDialogVisible.value = false
    await loadReports()
  } finally {
    reportSaving.value = false
  }
}

const handleToggleUserStatus = async row => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'

  await ElMessageBox.confirm(`确定${actionText}账号「${row.username}」吗？`, `${actionText}用户`, {
    type: 'warning',
    confirmButtonText: `确定${actionText}`,
    cancelButtonText: '取消'
  })

  userActionId.value = row.id
  try {
    await updateUserStatusApi(row.id, nextStatus)
    row.status = nextStatus
    ElMessage.success(`账号已${actionText}`)
  } finally {
    userActionId.value = null
  }
}

const handleDeleteUser = async row => {
  await ElMessageBox.confirm(
    `确定删除账号「${row.username}」吗？该用户的帖子、评论、收藏、点赞、好友关系、私聊、站内消息、举报记录以及头像背景等资源会一起删除，操作不可恢复。`,
    '删除用户',
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  )

  userActionId.value = row.id
  try {
    await deleteUserApi(row.id)
    ElMessage.success('用户已删除')
    if (userList.value.length === 1 && userPage.page > 1) {
      userPage.page -= 1
    }
    await Promise.all([loadUsers(), loadStats()])
  } finally {
    userActionId.value = null
  }
}

const openMessageDialog = () => {
  messageForm.userId = null
  messageForm.title = ''
  messageForm.content = ''
  messageForm.type = 'SYSTEM'
  messageForm.relatedId = null
  messageDialogVisible.value = true
}

const handleSendMessage = async () => {
  await messageFormRef.value.validate()

  messageSaving.value = true
  try {
    await sendMessageApi({
      userId: messageForm.userId,
      title: messageForm.title,
      content: messageForm.content,
      type: messageForm.type,
      relatedId: messageForm.relatedId || null
    })
    ElMessage.success('消息已发送')
    messageDialogVisible.value = false
    await loadAdminMessages()
  } finally {
    messageSaving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadStats(), loadAutoAuditConfig(), loadCategories(), loadActivitySubCategories(), loadAuditList()])
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 32px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(18px);
}

.brand,
.top-actions,
.hero-actions,
.admin-chip,
.auto-audit-card,
.panel-head,
.filter-line,
.form-row {
  display: flex;
  align-items: center;
}

.brand {
  gap: 12px;
  cursor: pointer;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
  font-size: 20px;
}

.brand h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
  line-height: 1.2;
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.top-actions {
  gap: 10px;
}

.admin-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  padding: 30px;
  border-radius: var(--radius);
  background: var(--hero-gradient), var(--primary);
  color: #fff;
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.82;
  font-weight: 700;
  text-transform: uppercase;
}

.hero h2 {
  margin: 0;
  font-size: 32px;
}

.hero p {
  margin: 10px 0 0;
  opacity: 0.86;
}

.admin-chip {
  min-width: 190px;
  gap: 10px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
}

.hero-actions {
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.auto-audit-card {
  min-width: 220px;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
}

.admin-chip strong,
.admin-chip span,
.auto-audit-card strong,
.auto-audit-card span {
  display: block;
}

.admin-chip span,
.auto-audit-card span {
  margin-top: 2px;
  font-size: 12px;
  opacity: 0.78;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.stat-card,
.workspace {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.stat-card {
  min-height: 118px;
  padding: 18px;
  display: grid;
  align-content: space-between;
}

.stat-card .el-icon {
  color: var(--primary);
  font-size: 24px;
}

.stat-card span,
.muted {
  color: var(--muted);
  font-size: 13px;
}

.stat-card strong {
  color: var(--primary-deep);
  font-size: 26px;
}

.workspace {
  margin-top: 20px;
  padding: 24px;
}

.workspace :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.workspace :deep(.el-tabs__item) {
  color: var(--muted);
}

.workspace :deep(.el-tabs__item.is-active),
.workspace :deep(.el-tabs__item:hover) {
  color: var(--primary);
}

.workspace :deep(.el-tabs__active-bar) {
  background: var(--primary);
}

.workspace :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: var(--surface-soft);
  --el-table-row-hover-bg-color: var(--surface-soft);
  --el-table-border-color: var(--border);
  --el-table-text-color: var(--text);
  --el-table-header-text-color: var(--muted);
  background: transparent;
  color: var(--text);
}

.workspace :deep(.el-table th.el-table__cell),
.workspace :deep(.el-table tr),
.workspace :deep(.el-table td.el-table__cell) {
  background: transparent;
}

.workspace :deep(.el-table th.el-table__cell) {
  background: var(--surface-soft);
}

.workspace :deep(.el-table__body-wrapper),
.workspace :deep(.el-table__inner-wrapper),
.workspace :deep(.el-table__empty-block) {
  background: transparent;
}

.workspace :deep(.el-table__empty-text) {
  color: var(--muted);
}

.workspace :deep(.el-input__wrapper),
.workspace :deep(.el-select__wrapper),
.workspace :deep(.el-input-number__decrease),
.workspace :deep(.el-input-number__increase) {
  background: var(--surface-soft);
  border-color: var(--border);
}

.workspace :deep(.el-input__inner),
.workspace :deep(.el-select__placeholder),
.workspace :deep(.el-select__selected-item) {
  color: var(--text);
}

.panel-head {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.sub-panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin: 28px 0 18px;
  padding-top: 22px;
  border-top: 1px solid var(--border);
}

.panel-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 22px;
}

.sub-panel-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 20px;
}

.panel-head p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.sub-panel-head p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.filter-line {
  gap: 12px;
  margin-bottom: 16px;
}

.hero-config-panel {
  margin-bottom: 18px;
  padding: 18px;
  border-radius: var(--radius);
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.hero-config-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.hero-config-head h4 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.hero-config-head p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.hero-config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 18px;
  align-items: start;
}

.filter-line .el-input {
  max-width: 360px;
}

.filter-line .el-select {
  width: 150px;
}

.admin-pagination {
  width: 100%;
  justify-content: center;
  margin-top: 18px;
}

.admin-pagination :deep(.el-pagination) {
  justify-content: center;
}

.user-link {
  padding: 0;
  font-weight: 700;
}

.title-with-pin {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.title-with-pin span:last-child {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.full-width {
  width: 100%;
}

.form-row {
  gap: 28px;
  align-items: flex-start;
}

.report-summary {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  margin-bottom: 14px;
  border-radius: 12px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.report-summary span {
  color: var(--muted);
  line-height: 1.6;
}

.dialog-tip {
  margin-bottom: 16px;
}

.hidden-input {
  display: none;
}

.notice-bg-editor {
  display: grid;
  gap: 12px;
}

.notice-bg-preview {
  position: relative;
  aspect-ratio: 3.36 / 1;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  padding: 14px;
  background:
    var(--hero-gradient),
    var(--primary);
  background-size: cover;
  background-position: center;
  color: #fff;
}

.notice-bg-preview span {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.46);
  font-size: 12px;
}

.notice-bg-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.notice-cropper {
  display: grid;
  gap: 18px;
}

.notice-crop-stage {
  position: relative;
  height: 420px;
  overflow: hidden;
  border-radius: 18px;
  background: var(--bg);
  cursor: grab;
  user-select: none;
}

.notice-crop-stage:active {
  cursor: grabbing;
}

.notice-crop-image {
  position: absolute;
  left: 50%;
  top: 50%;
  max-width: none;
  user-select: none;
  pointer-events: none;
}

.notice-crop-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.44);
}

.notice-crop-frame {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 560px;
  height: 167px;
  transform: translate(-50%, -50%);
  border: 2px solid #fff;
  border-radius: 16px;
  box-shadow: 0 0 0 999px rgba(0, 0, 0, 0.4);
}

.notice-crop-controls {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.field-help {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 980px) {
  .topbar,
  .hero,
  .panel-head,
  .sub-panel-head {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
  }

  .topbar {
    padding: 16px 22px;
  }

  .top-actions,
  .filter-line {
    width: 100%;
    flex-wrap: wrap;
  }

  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .hero,
  .workspace {
    padding: 22px;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }

  .filter-line .el-input,
  .filter-line .el-select,
  .filter-line .el-button {
    width: 100%;
    max-width: none;
  }
}
</style>

