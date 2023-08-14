# rn-android-sms-retriever

A react native wrapper for the SMS User Consent API to request user consent to read a single SMS verification message. If the user consents, the API returns the text of the message, from which you can get the verification code and complete the verification process.

## Installation

```sh
npm install rn-android-sms-retriever
```

## Usage

```js
import { multiply } from 'rn-android-sms-retriever';

// ...

const result = await multiply(3, 7);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
