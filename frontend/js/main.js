// Client-side UI Controller and Page Logic
document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initPage();
});

// Helper: Show Toast Notification
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Generate Common Navbar based on Auth State
function initNavbar() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    const user = Auth.getUser();
    const mode = Auth.getMode();
    const modeText = mode === 'token' ? 'JWT Token Mode' : 'Session Mode';
    
    let userNavHtml = '';
    if (user) {
        let dashboardUrl = '/dashboard-buyer.html';
        if (user.role === 'ROLE_ADMIN') dashboardUrl = '/dashboard-admin.html';
        if (user.role === 'ROLE_SELLER') dashboardUrl = '/dashboard-seller.html';
        
        userNavHtml = `
            <li class="nav-item">
                <span class="badge badge-info" style="margin-right: 10px;">${modeText}</span>
            </li>
            <li class="nav-item">
                <span style="color: var(--text-muted);">Hi, <strong>${user.username}</strong> (${user.role.replace('ROLE_', '')})</span>
            </li>
            <li class="nav-item">
                <a href="${dashboardUrl}" class="nav-link">Dashboard</a>
            </li>
            <li class="nav-item">
                <button onclick="Auth.logout()" class="btn btn-secondary btn-small">Sign Out</button>
            </li>
        `;
    } else {
        userNavHtml = `
            <li class="nav-item">
                <a href="/login.html" class="nav-link">Sign In</a>
            </li>
            <li class="nav-item">
                <a href="/register.html" class="btn btn-primary btn-small">Sign Up</a>
            </li>
        `;
    }

    navbar.innerHTML = `
        <a href="/index.html" class="nav-brand">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"/>
                <path d="M10 4v16M14 4v16M2 10h20M2 14h20"/>
            </svg>
            RailPass
        </a>
        <ul class="nav-links">
            <li class="nav-item"><a href="/index.html" class="nav-link">Home</a></li>
            ${userNavHtml}
        </ul>
    `;
}

// Init logic depending on the active HTML page
async function initPage() {
    const path = window.location.pathname;
    
    if (path.endsWith('index.html') || path === '/' || path === '') {
        initHomePage();
    } else if (path.endsWith('login.html')) {
        initLoginPage();
    } else if (path.endsWith('register.html')) {
        initRegisterPage();
    } else if (path.endsWith('train-detail.html')) {
        initTrainDetailPage();
    } else if (path.endsWith('dashboard-buyer.html')) {
        initBuyerDashboard();
    } else if (path.endsWith('dashboard-seller.html')) {
        initSellerDashboard();
    } else if (path.endsWith('dashboard-admin.html')) {
        initAdminDashboard();
    }
}

// --- Home Page Logic ---
async function initHomePage() {
    const searchForm = document.getElementById('search-form');
    const trainsGrid = document.getElementById('trains-grid');
    
    // Load and render trains
    const loadTrains = async (query = '') => {
        try {
            const res = await Auth.fetch(`/api/trains${query}`);
            if (res.ok) {
                const trains = await res.json();
                renderTrainsList(trains);
            }
        } catch (e) {
            console.error('Failed to load trains', e);
        }
    };

    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const departure = document.getElementById('departure').value;
            const arrival = document.getElementById('arrival').value;
            const date = document.getElementById('date').value;
            
            let query = '?';
            if (departure) query += `departure=${encodeURIComponent(departure)}&`;
            if (arrival) query += `arrival=${encodeURIComponent(arrival)}&`;
            if (date) query += `date=${encodeURIComponent(date)}&`;
            
            loadTrains(query);
        });
    }

    loadTrains();
}

function renderTrainsList(trains) {
    const grid = document.getElementById('trains-grid');
    if (!grid) return;

    if (trains.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 40px;">No train routes found matching your criteria.</div>';
        return;
    }

    grid.innerHTML = trains.map(train => {
        const isLowSeats = train.availableSeats < 10;
        return `
            <div class="train-card glass-card">
                <div class="train-header">
                    <span class="train-number">${train.trainNumber}</span>
                    <span class="train-price">${train.price.toLocaleString()} VND</span>
                </div>
                <div class="route-flow">
                    <div class="route-station">
                        <div class="station-name">${train.departureStation}</div>
                        <div class="station-time">${train.departureDate} @ ${train.departureTime.substring(0,5)}</div>
                    </div>
                    <div class="route-line"></div>
                    <div class="route-station">
                        <div class="station-name">${train.arrivalStation}</div>
                        <div class="station-time">${train.arrivalDate} @ ${train.arrivalTime.substring(0,5)}</div>
                    </div>
                </div>
                <div class="train-details">
                    <span>Seller: <strong>${train.sellerName}</strong></span>
                    <span class="available-seats-badge ${isLowSeats ? 'low' : ''}">
                        ${train.availableSeats} / ${train.totalSeats} seats left
                    </span>
                </div>
                <a href="/train-detail.html?id=${train.id}" class="btn btn-primary" style="width: 100%;">
                    Book Ticket
                </a>
            </div>
        `;
    }).join('');
}

// --- Login Page Logic ---
function initLoginPage() {
    const sessionTab = document.getElementById('tab-session');
    const tokenTab = document.getElementById('tab-token');
    const loginForm = document.getElementById('login-form');
    const terminal = document.getElementById('logger-terminal');

    const logToTerminal = (msg) => {
        if (!terminal) return;
        const now = new Date().toLocaleTimeString();
        terminal.innerHTML += `[${now}] ${msg}\n`;
        terminal.scrollTop = terminal.scrollHeight;
    };

    // Initialize Active Tab
    const mode = Auth.getMode();
    if (mode === 'token') {
        tokenTab.classList.add('active');
        sessionTab.classList.remove('active');
        logToTerminal('AUTH MODE: JWT Token authentication active.');
    } else {
        sessionTab.classList.add('active');
        tokenTab.classList.remove('active');
        logToTerminal('AUTH MODE: State-based session cookies (JSESSIONID) active.');
    }

    sessionTab.addEventListener('click', () => {
        Auth.setMode('session');
        sessionTab.classList.add('active');
        tokenTab.classList.remove('active');
        logToTerminal('SWITCH: Changed mode to Session Authentication.');
    });

    tokenTab.addEventListener('click', () => {
        Auth.setMode('token');
        tokenTab.classList.add('active');
        sessionTab.classList.remove('active');
        logToTerminal('SWITCH: Changed mode to JWT Token Authentication.');
    });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const currentMode = Auth.getMode();

        logToTerminal(`POST: Sending login request via API for user "${username}"...`);

        try {
            let res;
            if (currentMode === 'token') {
                res = await Auth.fetch('/api/auth/login/token', {
                    method: 'POST',
                    body: { username, password }
                });
                
                if (res.ok) {
                    const data = await res.json();
                    Auth.setToken(data.token);
                    Auth.setUser(data.user);
                    logToTerminal('SUCCESS: JWT generated successfully and saved in LocalStorage.');
                    logToTerminal(`JWT Payload: ${JSON.stringify(data.user)}`);
                    showToast('Successfully logged in with JWT!', 'success');
                    setTimeout(() => redirectUser(data.user), 1500);
                } else {
                    const err = await res.json();
                    logToTerminal(`ERROR: Login rejected by server. ${err.message || ''}`);
                    showToast(err.message || 'Login failed', 'error');
                }
            } else {
                res = await Auth.fetch('/api/auth/login/session', {
                    method: 'POST',
                    body: { username, password }
                });
                
                if (res.ok) {
                    const user = await res.json();
                    Auth.setUser(user);
                    logToTerminal('SUCCESS: Session established. Server set Cookie "JSESSIONID".');
                    showToast('Successfully logged in with Session Cookie!', 'success');
                    setTimeout(() => redirectUser(user), 1500);
                } else {
                    const err = await res.json();
                    logToTerminal(`ERROR: Session creation failed. ${err.message || ''}`);
                    showToast(err.message || 'Login failed', 'error');
                }
            }
        } catch (err) {
            logToTerminal(`CRITICAL ERROR: network connection failed.`);
        }
    });

    const params = new URLSearchParams(window.location.search);
    if (params.get('logout')) {
        logToTerminal('NOTICE: Logged out successfully. Cookies deleted / JWT cleared.');
    }
}

function redirectUser(user) {
    if (user.role === 'ROLE_ADMIN') {
        window.location.href = '/dashboard-admin.html';
    } else if (user.role === 'ROLE_SELLER') {
        window.location.href = '/dashboard-seller.html';
    } else {
        window.location.href = '/index.html';
    }
}

// --- Register Page Logic ---
function initRegisterPage() {
    const registerForm = document.getElementById('register-form');
    if (!registerForm) return;

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const role = document.getElementById('role').value;

        try {
            const res = await Auth.fetch('/api/auth/register', {
                method: 'POST',
                body: { username, email, password, role }
            });

            if (res.ok) {
                showToast('Registration successful! Redirecting to login...', 'success');
                setTimeout(() => {
                    window.location.href = '/login.html?registered=true';
                }, 2000);
            } else {
                const err = await res.json();
                showToast(err.message || 'Registration failed', 'error');
            }
        } catch (e) {
            showToast('Registration failed due to network error.', 'error');
        }
    });
}

// --- Train Detail & Interactive Seat Selection Page ---
let selectedSeats = [];
async function initTrainDetailPage() {
    const params = new URLSearchParams(window.location.search);
    const trainId = params.get('id');
    if (!trainId) {
        window.location.href = '/index.html';
        return;
    }

    const bookingForm = document.getElementById('booking-form');
    if (bookingForm) {
        // Intercept submit
        bookingForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const user = Auth.getUser();
            if (!user) {
                showToast('Please sign in to book tickets.', 'error');
                setTimeout(() => window.location.href = '/login.html', 1500);
                return;
            }
            if (user.role !== 'ROLE_BUYER') {
                showToast('Only buyers can book tickets! (Admins and Sellers are restricted)', 'error');
                return;
            }
            if (selectedSeats.length === 0) {
                showToast('Please select at least one seat.', 'error');
                return;
            }

            const passengerName = document.getElementById('passengerName').value;
            const passengerIdCard = document.getElementById('passengerIdCard').value;

            try {
                const res = await Auth.fetch(`/api/tickets/book/${trainId}`, {
                    method: 'POST',
                    body: {
                        selectedSeats,
                        passengerName,
                        passengerIdCard
                    }
                });

                if (res.ok) {
                    showToast('Ticket booked successfully!', 'success');
                    setTimeout(() => {
                        window.location.href = '/dashboard-buyer.html';
                    }, 1500);
                } else {
                    const err = await res.json();
                    showToast(err.message || 'Booking failed', 'error');
                }
            } catch (error) {
                showToast('Booking failed due to network error.', 'error');
            }
        });
    }

    await loadTrainDetail(trainId);
}

async function loadTrainDetail(trainId) {
    try {
        const res = await Auth.fetch(`/api/trains/${trainId}`);
        if (!res.ok) {
            showToast('Failed to load train details', 'error');
            return;
        }

        const data = await res.json();
        const train = data.train;
        const occupied = data.occupiedSeats;

        // Render Train Details
        document.getElementById('train-number-title').innerText = train.trainNumber;
        document.getElementById('route-display').innerText = `${train.departureStation} to ${train.arrivalStation}`;
        document.getElementById('departure-details').innerText = `${train.departureDate} @ ${train.departureTime.substring(0,5)}`;
        document.getElementById('arrival-details').innerText = `${train.arrivalDate} @ ${train.arrivalTime.substring(0,5)}`;
        document.getElementById('ticket-price').innerText = `${train.price.toLocaleString()} VND`;
        document.getElementById('seats-remaining').innerText = `${train.availableSeats} / ${train.totalSeats} seats remaining`;

        // Render Seat Grid
        const grid = document.getElementById('seat-map-grid');
        grid.innerHTML = '';

        for (let i = 1; i <= train.totalSeats; i++) {
            const isOccupied = occupied.includes(i);
            const seat = document.createElement('div');
            seat.className = `seat ${isOccupied ? 'occupied' : 'vacant'}`;
            seat.innerText = i;
            
            if (!isOccupied) {
                seat.addEventListener('click', () => {
                    if (seat.classList.contains('selected')) {
                        seat.classList.remove('selected');
                        selectedSeats = selectedSeats.filter(s => s !== i);
                    } else {
                        seat.classList.add('selected');
                        selectedSeats.push(i);
                    }
                    updateSelectedSeatsText(train.price);
                });
            }
            grid.appendChild(seat);
        }
    } catch (e) {
        showToast('Error fetching train info.', 'error');
    }
}

function updateSelectedSeatsText(pricePerSeat) {
    const listEl = document.getElementById('selected-seats-list');
    const totalEl = document.getElementById('total-cost-display');
    
    if (selectedSeats.length === 0) {
        listEl.innerText = 'None';
        totalEl.innerText = '0 VND';
        return;
    }

    listEl.innerText = selectedSeats.sort((a,b) => a-b).join(', ');
    totalEl.innerText = `${(selectedSeats.length * pricePerSeat).toLocaleString()} VND`;
}

// --- Buyer Dashboard Logic ---
async function initBuyerDashboard() {
    try {
        const res = await Auth.fetch('/api/dashboard/buyer');
        if (!res.ok) {
            showToast('Session expired. Redirecting...', 'error');
            setTimeout(() => window.location.href = '/login.html', 1500);
            return;
        }

        const data = await res.json();
        renderBuyerTickets(data.tickets);
    } catch (e) {
        showToast('Error loading dashboard.', 'error');
    }
}

function renderBuyerTickets(tickets) {
    const tbody = document.getElementById('buyer-tickets-body');
    if (!tbody) return;

    if (tickets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted);">You have not booked any tickets yet.</td></tr>';
        return;
    }

    tbody.innerHTML = tickets.map(t => {
        const isCancelled = t.status === 'CANCELLED';
        const cancelBtn = isCancelled 
            ? `<span class="badge badge-danger">Cancelled</span>`
            : `<button onclick="cancelTicket(${t.id})" class="btn btn-danger btn-small">Cancel / Refund</button>`;
        
        return `
            <tr>
                <td><strong>${t.trainNumber}</strong></td>
                <td>${t.departureStation} &rarr; ${t.arrivalStation}</td>
                <td>${t.departureDate} ${t.departureTime.substring(0,5)}</td>
                <td>Car ${t.carriageNumber}, Seat ${t.seatNumber}</td>
                <td>${t.passengerName}</td>
                <td>${t.passengerIdCard}</td>
                <td>${t.totalPrice.toLocaleString()} VND</td>
                <td><span class="badge ${isCancelled ? 'badge-danger' : 'badge-success'}">${t.status}</span></td>
                <td>${cancelBtn}</td>
            </tr>
        `;
    }).join('');
}

async function cancelTicket(ticketId) {
    if (!confirm('Are you sure you want to cancel this ticket and request a refund?')) return;

    try {
        const res = await Auth.fetch(`/api/tickets/cancel/${ticketId}`, {
            method: 'POST'
        });

        if (res.ok) {
            showToast('Ticket cancelled. Refund initiated.', 'success');
            initBuyerDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Cancellation failed', 'error');
        }
    } catch (e) {
        showToast('Cancellation failed due to network error.', 'error');
    }
}

// --- Seller Dashboard Logic ---
async function initSellerDashboard() {
    try {
        const res = await Auth.fetch('/api/dashboard/seller');
        if (!res.ok) {
            showToast('Session expired. Redirecting...', 'error');
            setTimeout(() => window.location.href = '/login.html', 1500);
            return;
        }

        const data = await res.json();
        
        // Render Stats
        document.getElementById('revenue-val').innerText = `${data.totalRevenue.toLocaleString()} VND`;
        document.getElementById('tickets-val').innerText = data.totalTicketsSold;
        document.getElementById('trains-val').innerText = data.trains.length;

        // Render routes options in modal dropdown
        const routeSelect = document.getElementById('train-route-select');
        if (routeSelect) {
            routeSelect.innerHTML = '<option value="">-- Choose Fixed Route --</option>' + 
                data.routes.map(r => `<option value="${r.id}">${r.departureStation} &rarr; ${r.arrivalStation}</option>`).join('');
        }

        renderSellerTrains(data.trains);
        renderSellerTickets(data.tickets);
        renderSellerRoutes(data.routes);

    } catch (e) {
        showToast('Error loading seller dashboard.', 'error');
    }
}

function renderSellerRoutes(routes) {
    const tbody = document.getElementById('seller-routes-body');
    if (!tbody) return;

    if (routes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No fixed routes created yet.</td></tr>';
        return;
    }

    tbody.innerHTML = routes.map(r => `
        <tr>
            <td>${r.id}</td>
            <td><strong>${r.departureStation}</strong></td>
            <td><strong>${r.arrivalStation}</strong></td>
            <td>
                <button onclick="deleteRoute(${r.id})" class="btn btn-danger btn-small">Delete</button>
            </td>
        </tr>
    `).join('');
}

function renderSellerTrains(trains) {
    const tbody = document.getElementById('seller-trains-body');
    if (!tbody) return;

    if (trains.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No train routes managed.</td></tr>';
        return;
    }

    tbody.innerHTML = trains.map(t => `
        <tr>
            <td><strong>${t.trainNumber}</strong></td>
            <td>${t.departureStation} &rarr; ${t.arrivalStation}</td>
            <td>${t.departureDate} ${t.departureTime.substring(0,5)}</td>
            <td>${t.arrivalDate} ${t.arrivalTime.substring(0,5)}</td>
            <td>${t.price.toLocaleString()} VND</td>
            <td>${t.availableSeats} / ${t.totalSeats}</td>
            <td>
                <button onclick="openEditTrainModal(${JSON.stringify(t).replace(/"/g, '&quot;')})" class="btn btn-secondary btn-small">Edit</button>
                <button onclick="deleteTrain(${t.id})" class="btn btn-danger btn-small">Delete</button>
            </td>
        </tr>
    `).join('');
}

function renderSellerTickets(tickets) {
    const tbody = document.getElementById('seller-tickets-body');
    if (!tbody) return;

    if (tickets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No tickets sold yet.</td></tr>';
        return;
    }

    tbody.innerHTML = tickets.map(t => `
        <tr>
            <td><strong>${t.trainNumber}</strong></td>
            <td>Car ${t.carriageNumber}, Seat ${t.seatNumber}</td>
            <td>${t.passengerName}</td>
            <td>${t.passengerIdCard}</td>
            <td>${t.totalPrice.toLocaleString()} VND</td>
            <td>${t.bookingDate.replace('T', ' ').substring(0,16)}</td>
            <td><span class="badge ${t.status === 'CANCELLED' ? 'badge-danger' : 'badge-success'}">${t.status}</span></td>
            <td>
                <span style="font-size:12px; color:var(--text-muted);">Owner cancel only</span>
            </td>
        </tr>
    `).join('');
}

// --- Seller Management Trigger ---
async function createRouteSubmit(e) {
    e.preventDefault();
    const departureStation = document.getElementById('route-departure').value;
    const arrivalStation = document.getElementById('route-arrival').value;

    try {
        const res = await Auth.fetch('/api/routes', {
            method: 'POST',
            body: { departureStation, arrivalStation }
        });

        if (res.ok) {
            showToast('Fixed route created!', 'success');
            document.getElementById('route-form').reset();
            initSellerDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to create route', 'error');
        }
    } catch (e) {
        showToast('Network error creating route.', 'error');
    }
}

async function deleteRoute(routeId) {
    if (!confirm('Are you sure you want to delete this fixed route?')) return;
    try {
        const res = await Auth.fetch(`/api/routes/${routeId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Route deleted.', 'success');
            initSellerDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Delete failed', 'error');
        }
    } catch (e) {
        showToast('Network error.', 'error');
    }
}

// Open modals
let editingTrainId = null;
function openAddTrainModal() {
    editingTrainId = null;
    document.getElementById('train-modal-title').innerText = 'Create Train Route';
    document.getElementById('train-form').reset();
    document.getElementById('train-modal').classList.add('active');
}

function openEditTrainModal(train) {
    editingTrainId = train.id;
    document.getElementById('train-modal-title').innerText = 'Edit Train Details';
    document.getElementById('train-number').value = train.trainNumber;
    document.getElementById('train-route-select').value = train.routeId;
    document.getElementById('train-dep-date').value = train.departureDate;
    document.getElementById('train-dep-time').value = train.departureTime.substring(0,5);
    document.getElementById('train-arr-date').value = train.arrivalDate;
    document.getElementById('train-arr-time').value = train.arrivalTime.substring(0,5);
    document.getElementById('train-price').value = train.price;
    document.getElementById('train-seats').value = train.totalSeats;
    
    document.getElementById('train-modal').classList.add('active');
}

function closeTrainModal() {
    document.getElementById('train-modal').classList.remove('active');
}

async function submitTrainForm(e) {
    e.preventDefault();
    const trainNumber = document.getElementById('train-number').value;
    const routeId = document.getElementById('train-route-select').value;
    const departureDate = document.getElementById('train-dep-date').value;
    const departureTime = document.getElementById('train-dep-time').value;
    const arrivalDate = document.getElementById('train-arr-date').value;
    const arrivalTime = document.getElementById('train-arr-time').value;
    const price = parseFloat(document.getElementById('train-price').value);
    const totalSeats = parseInt(document.getElementById('train-seats').value);

    const payload = {
        trainNumber,
        routeId: parseInt(routeId),
        departureDate,
        departureTime: departureTime + ':00',
        arrivalDate,
        arrivalTime: arrivalTime + ':00',
        price,
        totalSeats
    };

    try {
        const url = editingTrainId ? `/api/trains/${editingTrainId}` : '/api/trains';
        const method = editingTrainId ? 'PUT' : 'POST';

        const res = await Auth.fetch(url, {
            method,
            body: payload
        });

        if (res.ok) {
            showToast(editingTrainId ? 'Train updated successfully!' : 'Train created successfully!', 'success');
            closeTrainModal();
            initSellerDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to save train details', 'error');
        }
    } catch (e) {
        showToast('Network error saving train.', 'error');
    }
}

async function deleteTrain(trainId) {
    if (!confirm('Are you sure you want to delete this train schedule? (Cannot be undone and only allowed if no seats are booked)')) return;
    try {
        const res = await Auth.fetch(`/api/trains/${trainId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Train route deleted successfully.', 'success');
            initSellerDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to delete train.', 'error');
        }
    } catch (e) {
        showToast('Network error deleting train.', 'error');
    }
}

// --- Admin Dashboard Logic ---
async function initAdminDashboard() {
    try {
        const res = await Auth.fetch('/api/dashboard/admin');
        if (!res.ok) {
            showToast('Session expired. Redirecting...', 'error');
            setTimeout(() => window.location.href = '/login.html', 1500);
            return;
        }

        const data = await res.json();
        
        // Render Stats
        document.getElementById('admin-revenue-val').innerText = `${data.totalRevenue.toLocaleString()} VND`;
        document.getElementById('admin-bookings-val').innerText = data.totalBookings;
        document.getElementById('admin-buyers-val').innerText = data.totalBuyers;
        document.getElementById('admin-sellers-val').innerText = data.totalSellers;

        renderAdminUsers(data.users);
        renderAdminTrains(data.trains);
        renderAdminTickets(data.tickets);
    } catch (e) {
        showToast('Error loading administrative data.', 'error');
    }
}

function renderAdminUsers(users) {
    const tbody = document.getElementById('admin-users-body');
    if (!tbody) return;

    tbody.innerHTML = users.map(u => {
        const currentUser = Auth.getUser();
        const isSelf = currentUser && currentUser.id === u.id;
        const toggleBtn = isSelf 
            ? `<span style="font-size:12px; color:var(--text-muted);">Logged In</span>` 
            : `<button onclick="toggleUserStatus(${u.id})" class="btn btn-secondary btn-small">Toggle Active</button>`;
        
        let roleDropdown = `<span class="badge badge-info">${u.role.replace('ROLE_', '')}</span>`;
        if (!isSelf) {
            roleDropdown = `
                <select onchange="updateUserRole(${u.id}, this.value)" style="padding: 4px 8px; font-size: 13px; width: auto; display: inline-block;">
                    <option value="ROLE_BUYER" ${u.role === 'ROLE_BUYER' ? 'selected' : ''}>BUYER</option>
                    <option value="ROLE_SELLER" ${u.role === 'ROLE_SELLER' ? 'selected' : ''}>SELLER</option>
                    <option value="ROLE_ADMIN" ${u.role === 'ROLE_ADMIN' ? 'selected' : ''}>ADMIN</option>
                </select>
            `;
        }

        return `
            <tr>
                <td>${u.id}</td>
                <td><strong>${u.username}</strong></td>
                <td>${u.email}</td>
                <td>${roleDropdown}</td>
                <td>${toggleBtn}</td>
            </tr>
        `;
    }).join('');
}

function renderAdminTrains(trains) {
    const tbody = document.getElementById('admin-trains-body');
    if (!tbody) return;

    if (trains.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No trains on the network.</td></tr>';
        return;
    }

    tbody.innerHTML = trains.map(t => `
        <tr>
            <td><strong>${t.trainNumber}</strong></td>
            <td>${t.departureStation} &rarr; ${t.arrivalStation}</td>
            <td>${t.departureDate} ${t.departureTime.substring(0,5)}</td>
            <td>${t.price.toLocaleString()} VND</td>
            <td>${t.availableSeats} / ${t.totalSeats}</td>
            <td>Seller: ${t.sellerName}</td>
        </tr>
    `).join('');
}

function renderAdminTickets(tickets) {
    const tbody = document.getElementById('admin-tickets-body');
    if (!tbody) return;

    if (tickets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted);">No bookings placed yet.</td></tr>';
        return;
    }

    tbody.innerHTML = tickets.map(t => `
        <tr>
            <td><strong>${t.trainNumber}</strong></td>
            <td>${t.departureStation} &rarr; ${t.arrivalStation}</td>
            <td>Car ${t.carriageNumber}, Seat ${t.seatNumber}</td>
            <td>${t.passengerName}</td>
            <td>${t.passengerIdCard}</td>
            <td>${t.totalPrice.toLocaleString()} VND</td>
            <td><span class="badge ${t.status === 'CANCELLED' ? 'badge-danger' : 'badge-success'}">${t.status}</span></td>
            <td>
                <span style="font-size:12px; color:var(--text-muted);">Locked (Owner cancels only)</span>
            </td>
        </tr>
    `).join('');
}

async function toggleUserStatus(userId) {
    try {
        const res = await Auth.fetch(`/api/dashboard/admin/toggle-user/${userId}`, { method: 'POST' });
        if (res.ok) {
            showToast('User status updated.', 'success');
            initAdminDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Operation failed', 'error');
        }
    } catch (e) {
        showToast('Network error.', 'error');
    }
}

async function updateUserRole(userId, newRole) {
    try {
        const res = await Auth.fetch(`/api/dashboard/admin/update-role/${userId}?role=${newRole}`, { method: 'POST' });
        if (res.ok) {
            showToast('User role updated successfully.', 'success');
            initAdminDashboard();
        } else {
            const err = await res.json();
            showToast(err.message || 'Operation failed', 'error');
        }
    } catch (e) {
        showToast('Network error.', 'error');
    }
}
