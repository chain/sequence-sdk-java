# Sequence Java SDK changelog

## 1.5.2 (201803??)

* The `assetTags`, `sourceAccountTags`, and `destinationAccountTags` properties
  on `ActionSum` have been deprecated; the new property is `snapshot`.
  Use `snapshot.FlavorTags` instead of `assetTags`.
  Use `snapshot.SourceAccountTags` and `snapshot.destinationAccountTags`.
* The `referenceData` property on `ActionSum` has been deprecated; the new
  property is `tags`.
* The `assetAlias` and `assetId` properties on `ActionSum` have been deprecated;
  the new property is `flavorId`.

## 1.5.1 (20180320)

* The `addKeyById` method on `Account.Builder` and `Flavor.Builder` has been
  deprecated; the new method is `addKeyId`.
* The `addSourceContractId` method on `Transaction.Builder.Action.Transfer`
  and `Transaction.Builder.Action.Retire` has been deprecated.

## 1.5 (20180316)

For full details on the 1.5 release and how to migrate your code,
[visit the Sequence changelog](https://dashboard.seq.com/docs/changelog#release-v1-5).

* Added `Feed`s. [More info](https://dashboard.seq.com/docs/feeds)
* The `keys` field on `Account` and `Flavor` has been deprecated; the new field
  is `keyIds`, containing key ID strings.
* Added support for camel-case identifiers in filter queries.
  Identifiers in query strings are now consistent with
  identifiers in Java source code.
  Snake-case names in query strings are deprecated.
* Transaction reference data properties and methods have been deprecated;
  Use action tags instead.

## 1.4 (20180308)

For full details on the 1.4 release and how to migrate your code,
[visit the Sequence changelog](https://dashboard.seq.com/docs/changelog#release-v1-4).

* Added `tags` to `Action`.
* Added `setActionTags` and `addActionTagsField` to `Transaction`
  action builders.
* Added timestamp inequalities in filters.
* `setReferenceData` and `addReferenceDataField` on `Transaction` builder actions
  have been deprecated. Use the new action tag fields above.
* `setStartTime` and `setEndTime` in Transaction filters have been deprecated.
  You can now use `"timestamp >= $1"` in a filter instead.

## 1.3 (20180301)

For full details on the 1.3 release and how to migrate your code,
[visit the Sequence changelog](https://dashboard.seq.com/docs/changelog#release-v1-3).

* Added new interfaces: listing tokens and summing tokens.
  Listing tokens replaces querying contracts, which is now deprecated.
  Summing tokens replaces querying balances, which is now deprecated.
* Added new interfaces: token tags.
  Set token tags on Issue and Transfer actions.
* Added new interface: `setFilter` and `addFilterParameter` on the transaction
  builder. Filter tokens by tags on Transfer and Retire actions.
* Added new pagination interfaces: accounts, actions, flavors, keys, tokens,
  transactions. Can be used in an end-user application, such as infinite scroll.
  Use `setPageSize(size)` on builders to set page size.
  Use `getPage(client, cursor)` to make subsequent requests.

## 1.2.0 (20180216)

For full details on the 1.2 release and how to migrate your code,
[visit the Sequence changelog](https://dashboard.seq.com/docs/changelog#release-v1-2).

* `Asset` has been renamed to `Flavor`; all references to assets have been
  deprecated.
* The `code` field on API errors has been deprecated; the new field is
  `seqCode`, containing `SEQXXX` error codes.
* The `sourceAccountTags`, `destinationAccountTags`, and `assetTags` on
  action objects have been deprecated; All tags on actions are now available
  within a new `Action.snapshot` object.

## 1.1.0 (20180206)

For full details on the 1.1 release and how to migrate your code,
[visit the Sequence changelog](https://dashboard.seq.com/docs/changelog#release-v1-1).

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
