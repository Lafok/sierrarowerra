# Sierra Rowerra
### POSTGRES
#### Port for DB changed to 5433!!!
```bash
  docker-compose up -d
```

### Stripe
```bash
  .\stripe.exe listen --forward-to localhost:8080/api/v1/stripe/webhook
```