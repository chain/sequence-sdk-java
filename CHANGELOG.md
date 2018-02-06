# Sequence Java SDK changelog

## 1.1.0 (20180206)

For full details on the 1.1 release of Sequence,
[visit the changelog](https://dashboard.seq.com/docs/changelog#release-v1-1).

* Added support for setting a user-provided id on key and account objects.
* The `alias` field on key and account objects has been deprecated.
* The `ledger` field when creating an API client has been deprecated; the new
  field is named `ledgerName`.
* Added full support for for listing and summing actions.

## 1.0.4 (20180122)

* Added Action class.
* Added groupBy field in Query class.
* Added fix for trailing slash errors.
* Improved retry logic for network errors.

## 1.0.3 (20171023)

* Added support for macaroon based authentication

## 1.0.2 (20170921)

* Removed the `ttl` and `setTtl` members of `Transaction.Builder`.

## 1.0.1 (20170920)

* Added `Balance#addSumByField` convenience method.
