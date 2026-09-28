const modules = {
  clubs: {
    title: 'Clubs',
    description: 'Keep club details and coordinators in one place.',
    endpoint: '/api/clubs',
    id: 'clubId',
    noun: 'club',
    columns: [
      { key: 'clubName', label: 'Club name' }, { key: 'category', label: 'Category' },
      { key: 'coordinatorName', label: 'Coordinator' }, { key: 'contactEmail', label: 'Email' }
    ],
    fields: [
      { key: 'clubName', label: 'Club name', required: true },
      { key: 'category', label: 'Category', required: true },
      { key: 'coordinatorName', label: 'Coordinator name', required: true },
      { key: 'contactEmail', label: 'Contact email', type: 'email', required: true },
      { key: 'description', label: 'Description', type: 'textarea', required: true, full: true }
    ]
  },
  members: {
    title: 'Members',
    description: 'Manage student membership and club affiliation.',
    endpoint: '/api/members',
    id: 'memberId',
    noun: 'member',
    columns: [
      { key: 'name', label: 'Name' }, { key: 'email', label: 'Email' },
      { key: 'department', label: 'Department' }, { key: 'year', label: 'Year' },
      { key: 'phoneNumber', label: 'Phone' }, { key: 'clubId', label: 'Club', relation: 'clubs' }
    ],
    fields: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'department', label: 'Department', required: true },
      { key: 'year', label: 'Year', type: 'number', min: 1, max: 6, required: true },
      { key: 'phoneNumber', label: 'Phone number', type: 'tel', required: true },
      { key: 'clubId', label: 'Club', type: 'relation', source: 'clubs', required: true }
    ]
  },
  events: {
    title: 'Events',
    description: 'Coordinate dates, venues, and the clubs hosting them.',
    endpoint: '/api/events',
    id: 'eventId',
    noun: 'event',
    columns: [
      { key: 'eventName', label: 'Event' }, { key: 'eventDate', label: 'Date', type: 'date' },
      { key: 'venue', label: 'Venue' }, { key: 'clubId', label: 'Hosted by', relation: 'clubs' }
    ],
    fields: [
      { key: 'eventName', label: 'Event name', required: true },
      { key: 'eventDate', label: 'Event date', type: 'date', required: true },
      { key: 'venue', label: 'Venue', required: true },
      { key: 'clubId', label: 'Hosting club', type: 'relation', source: 'clubs', required: true },
      { key: 'description', label: 'Description', type: 'textarea', required: true, full: true }
    ]
  },
  registrations: {
    title: 'Registrations',
    description: 'Track event sign-ups and their current status.',
    endpoint: '/api/registrations',
    id: 'registrationId',
    noun: 'registration',
    columns: [
      { key: 'memberId', label: 'Member', relation: 'members' },
      { key: 'eventId', label: 'Event', relation: 'events' },
      { key: 'registrationDate', label: 'Registered', type: 'date' },
      { key: 'status', label: 'Status' }
    ],
    fields: [
      { key: 'memberId', label: 'Member', type: 'relation', source: 'members', required: true },
      { key: 'eventId', label: 'Event', type: 'relation', source: 'events', required: true },
      { key: 'registrationDate', label: 'Registration date', type: 'date', required: true },
      { key: 'status', label: 'Status', type: 'select', options: ['Pending', 'Approved', 'Cancelled'], required: true }
    ]
  }
};

const state = { page: 'dashboard', data: {}, editingId: null };
const pageContent = document.querySelector('#page-content');
const dialog = document.querySelector('#record-dialog');
const form = document.querySelector('#record-form');
const fieldsContainer = document.querySelector('#form-fields');
let toastTimer;

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `Request failed (${response.status})`);
  }
  return response.status === 204 ? null : response.json();
}

async function refreshData() {
  const entries = Object.entries(modules);
  const records = await Promise.all(entries.map(([, config]) => request(config.endpoint)));
  entries.forEach(([key], index) => { state.data[key] = records[index]; });
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (character) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function relationName(source, id) {
  const record = (state.data[source] || []).find((item) => item[modules[source].id] === id);
  if (!record) return id ? `#${id}` : '—';
  const nameKey = source === 'clubs' ? 'clubName' : source === 'members' ? 'name' : 'eventName';
  return record[nameKey];
}

function formatDate(value) {
  if (!value) return '—';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat(undefined, {
    day: 'numeric', month: 'short', year: 'numeric'
  }).format(date);
}

function currentDateValue() {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function render() {
  document.querySelectorAll('.nav-item').forEach((button) => {
    button.classList.toggle('active', button.dataset.page === state.page);
  });
  const current = state.page === 'dashboard' ? 'Overview' : modules[state.page].title;
  document.querySelector('#current-section').textContent = current;
  if (state.page === 'dashboard') renderDashboard();
  else renderModule(state.page);
}

function renderDashboard() {
  const stats = [
    ['clubs', 'Total clubs', 'Registered communities', 'var(--green)'],
    ['members', 'Total members', 'Students in clubs', 'var(--orange)'],
    ['events', 'Total events', 'Scheduled activities', 'var(--blue)'],
    ['registrations', 'Registrations', 'Event sign-ups', 'var(--yellow)']
  ];
  const today = currentDateValue();
  const eventRows = [...(state.data.events || [])]
    .filter((event) => event.eventDate >= today)
    .sort((first, second) => (first.eventDate || '').localeCompare(second.eventDate || ''))
    .slice(0, 5);
  const clubRows = (state.data.clubs || []).slice(0, 5);
  pageContent.innerHTML = `
    <div class="page-heading">
      <div><div class="eyebrow">CAMPUS OVERVIEW</div><h1>Dashboard</h1><p>A clear view of clubs, people, and what’s on.</p></div>
      <button class="button button-primary" data-create="clubs"><span class="button-symbol">＋</span> Add a club</button>
    </div>
    <div class="stats-grid">${stats.map(([key, label, foot, color]) => `
      <button class="stat-card" style="--stat-color:${color}" data-page="${key}">
        <div class="stat-label">${label}</div><div class="stat-value">${(state.data[key] || []).length}</div><div class="stat-foot">${foot}</div>
      </button>`).join('')}
    </div>
    <div class="dashboard-grid">
      <section class="panel">
        <div class="panel-heading"><h2>Upcoming events</h2><button data-page="events">All events →</button></div>
        <div class="event-list">${eventRows.length ? eventRows.map((event) => {
          const date = new Date(`${event.eventDate}T00:00:00`);
          const day = Number.isNaN(date.getTime()) ? '—' : date.getDate();
          const month = Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString(undefined, { month: 'short' });
          return `<div class="event-row"><div class="date-block"><strong>${day}</strong><span>${month}</span></div><div class="event-copy"><strong>${escapeHtml(event.eventName)}</strong><span>${escapeHtml(event.venue)} · ${escapeHtml(relationName('clubs', event.clubId))}</span></div></div>`;
        }).join('') : '<div class="empty-state">No events have been added yet.</div>'}</div>
      </section>
      <section class="panel">
        <div class="panel-heading"><h2>Clubs</h2><button data-page="clubs">Manage →</button></div>
        <div class="club-list">${clubRows.length ? clubRows.map((club) => {
          const memberCount = (state.data.members || []).filter((member) => member.clubId === club.clubId).length;
          return `<div class="club-row"><div><strong>${escapeHtml(club.clubName)}</strong><span>${escapeHtml(club.category)}</span></div><span class="count-pill" title="Members">${memberCount}</span></div>`;
        }).join('') : '<div class="empty-state">Add a club to get started.</div>'}</div>
      </section>
    </div>`;
}

function getCellValue(record, column) {
  const value = record[column.key];
  if (column.relation) return escapeHtml(relationName(column.relation, value));
  if (column.type === 'date') return escapeHtml(formatDate(value));
  return escapeHtml(value || '—');
}

function renderModule(key) {
  const config = modules[key];
  const records = state.data[key] || [];
  const columns = config.columns;
  pageContent.innerHTML = `
    <div class="page-heading">
      <div><div class="eyebrow">CLUB MANAGEMENT</div><h1>${config.title}</h1><p>${config.description}</p></div>
      <button class="button button-primary" data-create="${key}"><span class="button-symbol">＋</span> Add ${config.noun}</button>
    </div>
    <div class="module-toolbar"><span class="record-count">${records.length} ${records.length === 1 ? 'record' : 'records'}</span></div>
    <div class="table-wrap"><table>
      <thead><tr>${columns.map((column) => `<th>${column.label}</th>`).join('')}<th><span class="sr-only">Actions</span></th></tr></thead>
      <tbody>${records.length ? records.map((record) => `<tr>
        ${columns.map((column) => `<td>${getCellValue(record, column)}</td>`).join('')}
        <td><div class="row-actions"><button class="text-action" data-edit="${escapeHtml(record[config.id])}">Edit</button><button class="text-action danger" data-delete="${escapeHtml(record[config.id])}">Delete</button></div></td>
      </tr>`).join('') : `<tr><td colspan="${columns.length + 1}"><div class="empty-state"><strong>No ${config.title.toLowerCase()} yet</strong>New records will appear here.</div></td></tr>`}</tbody>
    </table></div>`;
}

function fieldMarkup(field, current = {}) {
  const defaultValue = field.key === 'registrationDate' ? currentDateValue() : '';
  const value = current[field.key] ?? defaultValue;
  const required = field.required ? 'required' : '';
  const className = field.full ? 'field full' : 'field';
  let control;
  if (field.type === 'textarea') {
    control = `<textarea id="field-${field.key}" name="${field.key}" ${required}>${escapeHtml(value)}</textarea>`;
  } else if (field.type === 'relation') {
    const sourceConfig = modules[field.source];
    const labelKey = field.source === 'clubs' ? 'clubName' : field.source === 'members' ? 'name' : 'eventName';
    const options = (state.data[field.source] || []).map((record) => {
      const id = record[sourceConfig.id];
      const selected = String(id) === String(value) ? 'selected' : '';
      return `<option value="${escapeHtml(id)}" ${selected}>${escapeHtml(record[labelKey])}</option>`;
    }).join('');
    control = `<select id="field-${field.key}" name="${field.key}" ${required}><option value="">Choose ${field.label.toLowerCase()}</option>${options}</select>`;
  } else if (field.type === 'select') {
    control = `<select id="field-${field.key}" name="${field.key}" ${required}><option value="">Choose ${field.label.toLowerCase()}</option>${field.options.map((option) => `<option value="${escapeHtml(option)}" ${option === value ? 'selected' : ''}>${escapeHtml(option)}</option>`).join('')}</select>`;
  } else {
    const min = field.min ? `min="${field.min}"` : '';
    const max = field.max ? `max="${field.max}"` : '';
    control = `<input id="field-${field.key}" name="${field.key}" type="${field.type || 'text'}" value="${escapeHtml(value)}" ${required} ${min} ${max}>`;
  }
  return `<div class="${className}"><label for="field-${field.key}">${field.label}${field.required ? ' *' : ''}</label>${control}</div>`;
}

function openForm(key, record = null) {
  const config = modules[key];
  state.page = key;
  state.editingId = record?.[config.id] ?? null;
  document.querySelector('#dialog-eyebrow').textContent = state.editingId ? 'EDIT RECORD' : 'NEW RECORD';
  document.querySelector('#dialog-title').textContent = `${state.editingId ? 'Edit' : 'Add'} ${config.noun}`;
  document.querySelector('#save-record').textContent = state.editingId ? 'Save changes' : 'Save record';
  fieldsContainer.innerHTML = config.fields.map((field) => fieldMarkup(field, record || {})).join('');
  dialog.showModal();
}

function showToast(message, isError = false) {
  const toast = document.querySelector('#toast');
  toast.textContent = message;
  toast.classList.toggle('error', isError);
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 3200);
}

document.addEventListener('click', async (event) => {
  const pageButton = event.target.closest('[data-page]');
  if (pageButton) {
    state.page = pageButton.dataset.page;
    render();
    return;
  }
  const createButton = event.target.closest('[data-create]');
  if (createButton) {
    openForm(createButton.dataset.create);
    return;
  }
  if (event.target.closest('#close-dialog, #cancel-dialog')) {
    dialog.close();
    return;
  }
  const editButton = event.target.closest('[data-edit]');
  if (editButton) {
    const config = modules[state.page];
    const record = (state.data[state.page] || []).find((item) => String(item[config.id]) === editButton.dataset.edit);
    if (record) openForm(state.page, record);
    return;
  }
  const deleteButton = event.target.closest('[data-delete]');
  if (deleteButton) {
    const key = state.page;
    const config = modules[key];
    if (!window.confirm(`Delete this ${config.noun}?`)) return;
    try {
      await request(`${config.endpoint}/${deleteButton.dataset.delete}`, { method: 'DELETE' });
      await refreshData();
      render();
      showToast(`${config.title.slice(0, -1)} deleted.`);
    } catch (error) {
      showToast(error.message, true);
    }
  }
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const key = state.page;
  const config = modules[key];
  const body = Object.fromEntries(new FormData(form).entries());
  config.fields.forEach((field) => {
    if (field.type === 'number' || field.type === 'relation') {
      body[field.key] = body[field.key] === '' ? null : Number(body[field.key]);
    }
  });
  const url = state.editingId ? `${config.endpoint}/${state.editingId}` : config.endpoint;
  try {
    await request(url, { method: state.editingId ? 'PUT' : 'POST', body: JSON.stringify(body) });
    dialog.close();
    await refreshData();
    state.page = key;
    render();
    showToast(`${config.noun[0].toUpperCase()}${config.noun.slice(1)} saved.`);
  } catch (error) {
    showToast(error.message, true);
  }
});

document.querySelector('.main-nav').addEventListener('click', (event) => {
  const button = event.target.closest('[data-page]');
  if (button) {
    state.page = button.dataset.page;
    render();
  }
});

refreshData().then(render).catch((error) => {
  pageContent.innerHTML = `<div class="page-heading"><div><div class="eyebrow">CONNECTION</div><h1>Dashboard</h1><p>Could not load data from the application.</p></div></div><section class="panel"><div class="empty-state"><strong>API connection failed</strong>${escapeHtml(error.message)}</div></section>`;
});