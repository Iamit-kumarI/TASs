# TASs
User clicks "Buy"
↓
Load Balancer
↓
TSAS receives request
↓
Classify request
↓
Check rate limits
↓
Check backend capacity
↓
If free → dispatch immediately
Else → enqueue
↓
Worker sends request to backend
↓
Backend processes request
↓
Response flows back to user
