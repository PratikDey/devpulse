# DevPulse - Setup & Connection Guide

## 🔗 Frontend-Backend Connection

The frontend is now **fully connected** to the backend using a Vite proxy configuration. No CORS issues!

### How It Works

1. **Vite Proxy** - Frontend requests to `/api/*` are automatically forwarded to `http://localhost:8084`
2. **No CORS Issues** - The proxy makes requests appear same-origin
3. **No Backend Changes** - All configuration is on the frontend side

## 🚀 Quick Start

### 1. Start Backend Services

```bash
cd /Users/pratikdey/Workspace/Projects/devpulse/infra
docker-compose up --build
```

**Services Started:**
- 🗄️ MongoDB (port 27017) - Log storage
- 📨 Kafka (port 9092) - Message bus
- 🔧 Zookeeper (port 2181) - Kafka coordination
- 📊 Log Dashboard API (port 8084) - REST API + SSE
- 📦 Producer Services - Generate sample logs
- 📥 Log Collector - Consumes and stores logs
- 🚨 Alert Processor (port 8085) - Evaluates alert rules

### 2. Start Frontend

```bash
cd /Users/pratikdey/Workspace/Projects/devpulse/frontend
npm run dev
```

**Frontend runs on:** `http://localhost:5173`

The browser will automatically open with the DevPulse dashboard.

## ✅ Testing the Connection

### Check Backend is Running

```bash
# Test log dashboard API
curl http://localhost:8084/api/logs/recent

# Should return JSON with logs
```

### Check Frontend Connection

1. Open `http://localhost:5173` in browser
2. Navigate to **Dashboard** (default view)
3. You should see logs loaded from backend
4. Try filtering by service or log level
5. Navigate to **Live Stream** to see real-time logs via SSE

### Expected Behavior

✅ Dashboard shows paginated logs  
✅ Filters work (service, level)  
✅ Pagination works (Previous/Next)  
✅ Live Stream shows real-time updates  
✅ Live indicator shows "LIVE" when connected  

## 🔧 Configuration Files

### [vite.config.js](file:///Users/pratikdey/Workspace/Projects/devpulse/frontend/vite.config.js)
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8084',
      changeOrigin: true,
      secure: false,
    }
  }
}
```

### [api.js](file:///Users/pratikdey/Workspace/Projects/devpulse/frontend/src/utils/api.js)
```javascript
// Uses empty string for API_BASE_URL
// Vite proxy handles forwarding to backend
const API_BASE_URL = import.meta.env.VITE_API_URL || '';
```

## 🐛 Troubleshooting

### No Logs Showing?

1. **Check backend is running:**
   ```bash
   curl http://localhost:8084/api/logs/recent
   ```

2. **Check Docker services:**
   ```bash
   docker-compose ps
   ```
   All services should be "Up"

3. **Check MongoDB has logs:**
   ```bash
   docker exec -it devpulse-mongo mongosh
   use devpulse_logs
   db.logs.count()
   ```

### Frontend Can't Connect?

1. **Check Vite dev server is running** on port 5173
2. **Check proxy configuration** in vite.config.js
3. **Check browser console** for errors (F12)
4. **Try hard refresh**: Cmd+Shift+R (Mac) or Ctrl+Shift+R (Windows)

### No Live Stream Updates?

1. **Check SSE endpoint:**
   ```bash
   curl http://localhost:8084/api/logs/stream
   ```

2. **Verify producers are running** and generating logs
3. **Check log-collector is consuming** from Kafka

## 📋 API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/logs` | GET | Paginated logs (default) |
| `/api/logs/service/{name}` | GET | Filter by service |
| `/api/logs/level/{level}` | GET | Filter by log level |
| `/api/logs/recent` | GET | Top 100 recent logs |
| `/api/logs/stream` | GET | SSE real-time stream |

## 🎯 Next Steps

✅ **Connection Complete** - Frontend and backend are fully integrated  
✅ **No CORS Issues** - Vite proxy handles everything  
✅ **Real-time Updates** - SSE streaming works  
✅ **Zero Backend Changes** - All configuration is frontend-side  

The application is ready to use! 🎉
