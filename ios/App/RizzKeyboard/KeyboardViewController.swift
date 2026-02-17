import UIKit

class KeyboardViewController: UIInputViewController {
    
    private var suggestionsView: UIStackView!
    private var mainStack: UIStackView!
    private var inputField: UITextField!
    private var generateButton: UIButton!
    private var statusLabel: UILabel!
    private var suggestionsScroll: UIScrollView!
    private var suggestionsStack: UIStackView!
    private var isLoading = false
    
    private let groqKey: String = {
        let parts = ["gsk_2oDA7","TXmZY4Nbr","S4DZFjWGdy","b3FYnKvfx3","Man4P79WIR","J2xGprXX"]
        return parts.joined()
    }()
    
    private let bgColor = UIColor(red: 10/255, green: 10/255, blue: 15/255, alpha: 1)
    private let cardColor = UIColor(red: 26/255, green: 26/255, blue: 38/255, alpha: 1)
    private let accentColor = UIColor(red: 168/255, green: 85/255, blue: 247/255, alpha: 1)
    private let textColor = UIColor(red: 240/255, green: 240/255, blue: 245/255, alpha: 1)
    private let text2Color = UIColor(red: 152/255, green: 152/255, blue: 176/255, alpha: 1)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
    }
    
    private func setupUI() {
        guard let inputView = self.inputView else { return }
        inputView.backgroundColor = bgColor
        
        let heightConstraint = inputView.heightAnchor.constraint(equalToConstant: 260)
        heightConstraint.priority = .required
        heightConstraint.isActive = true
        
        mainStack = UIStackView()
        mainStack.axis = .vertical
        mainStack.spacing = 8
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        inputView.addSubview(mainStack)
        
        NSLayoutConstraint.activate([
            mainStack.topAnchor.constraint(equalTo: inputView.topAnchor, constant: 8),
            mainStack.leadingAnchor.constraint(equalTo: inputView.leadingAnchor, constant: 10),
            mainStack.trailingAnchor.constraint(equalTo: inputView.trailingAnchor, constant: -10),
            mainStack.bottomAnchor.constraint(lessThanOrEqualTo: inputView.bottomAnchor, constant: -8)
        ])
        
        let headerStack = UIStackView()
        headerStack.axis = .horizontal
        headerStack.spacing = 8
        headerStack.alignment = .center
        
        let logo = UILabel()
        logo.text = "⚡ RizzGPT"
        logo.font = .systemFont(ofSize: 14, weight: .bold)
        logo.textColor = accentColor
        
        let switchBtn = UIButton(type: .system)
        switchBtn.setTitle("🌐", for: .normal)
        switchBtn.titleLabel?.font = .systemFont(ofSize: 18)
        switchBtn.addTarget(self, action: #selector(switchKeyboard), for: .touchUpInside)
        
        let spacer = UIView()
        spacer.setContentHuggingPriority(.defaultLow, for: .horizontal)
        
        headerStack.addArrangedSubview(logo)
        headerStack.addArrangedSubview(spacer)
        headerStack.addArrangedSubview(switchBtn)
        mainStack.addArrangedSubview(headerStack)
        
        let modeScroll = UIScrollView()
        modeScroll.showsHorizontalScrollIndicator = false
        modeScroll.heightAnchor.constraint(equalToConstant: 32).isActive = true
        
        let modeStack = UIStackView()
        modeStack.axis = .horizontal
        modeStack.spacing = 6
        modeStack.translatesAutoresizingMaskIntoConstraints = false
        modeScroll.addSubview(modeStack)
        NSLayoutConstraint.activate([
            modeStack.topAnchor.constraint(equalTo: modeScroll.topAnchor),
            modeStack.leadingAnchor.constraint(equalTo: modeScroll.leadingAnchor),
            modeStack.trailingAnchor.constraint(equalTo: modeScroll.trailingAnchor),
            modeStack.bottomAnchor.constraint(equalTo: modeScroll.bottomAnchor),
            modeStack.heightAnchor.constraint(equalTo: modeScroll.heightAnchor)
        ])
        
        let modes = [("😏 Smooth", "smooth"), ("😂 Funny", "funny"), ("🔥 Bold", "bold"), ("💜 Flirty", "flirty"), ("💀 Savage", "savage"), ("🍯 Sweet", "sweet")]
        for (title, tag) in modes {
            let btn = UIButton(type: .system)
            btn.setTitle(title, for: .normal)
            btn.titleLabel?.font = .systemFont(ofSize: 12, weight: .semibold)
            btn.setTitleColor(text2Color, for: .normal)
            btn.setTitleColor(accentColor, for: .selected)
            btn.backgroundColor = cardColor
            btn.layer.cornerRadius = 14
            btn.layer.borderWidth = 1
            btn.layer.borderColor = UIColor.white.withAlphaComponent(0.06).cgColor
            btn.contentEdgeInsets = UIEdgeInsets(top: 6, left: 12, bottom: 6, right: 12)
            btn.tag = modes.firstIndex(where: { $0.1 == tag }) ?? 0
            btn.addTarget(self, action: #selector(modeSelected(_:)), for: .touchUpInside)
            if tag == "smooth" {
                btn.isSelected = true
                btn.backgroundColor = accentColor.withAlphaComponent(0.15)
                btn.layer.borderColor = accentColor.withAlphaComponent(0.3).cgColor
            }
            modeStack.addArrangedSubview(btn)
        }
        mainStack.addArrangedSubview(modeScroll)
        
        let inputRow = UIStackView()
        inputRow.axis = .horizontal
        inputRow.spacing = 8
        inputRow.alignment = .center
        
        inputField = UITextField()
        inputField.placeholder = "Their message or context..."
        inputField.font = .systemFont(ofSize: 14)
        inputField.textColor = textColor
        inputField.backgroundColor = cardColor
        inputField.layer.cornerRadius = 18
        inputField.layer.borderWidth = 1
        inputField.layer.borderColor = UIColor.white.withAlphaComponent(0.06).cgColor
        inputField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 14, height: 0))
        inputField.leftViewMode = .always
        inputField.rightView = UIView(frame: CGRect(x: 0, y: 0, width: 14, height: 0))
        inputField.rightViewMode = .always
        inputField.heightAnchor.constraint(equalToConstant: 38).isActive = true
        inputField.attributedPlaceholder = NSAttributedString(
            string: "Their message or context...",
            attributes: [.foregroundColor: text2Color]
        )
        
        generateButton = UIButton(type: .system)
        generateButton.setTitle("⚡", for: .normal)
        generateButton.titleLabel?.font = .systemFont(ofSize: 20)
        generateButton.backgroundColor = accentColor
        generateButton.layer.cornerRadius = 19
        generateButton.widthAnchor.constraint(equalToConstant: 38).isActive = true
        generateButton.heightAnchor.constraint(equalToConstant: 38).isActive = true
        generateButton.addTarget(self, action: #selector(generateTapped), for: .touchUpInside)
        
        inputRow.addArrangedSubview(inputField)
        inputRow.addArrangedSubview(generateButton)
        mainStack.addArrangedSubview(inputRow)
        
        statusLabel = UILabel()
        statusLabel.text = "Type their message → tap ⚡ → get rizz"
        statusLabel.font = .systemFont(ofSize: 11, weight: .medium)
        statusLabel.textColor = text2Color
        statusLabel.textAlignment = .center
        mainStack.addArrangedSubview(statusLabel)
        
        suggestionsScroll = UIScrollView()
        suggestionsScroll.showsVerticalScrollIndicator = false
        suggestionsScroll.heightAnchor.constraint(equalToConstant: 120).isActive = true
        
        suggestionsStack = UIStackView()
        suggestionsStack.axis = .vertical
        suggestionsStack.spacing = 6
        suggestionsStack.translatesAutoresizingMaskIntoConstraints = false
        suggestionsScroll.addSubview(suggestionsStack)
        NSLayoutConstraint.activate([
            suggestionsStack.topAnchor.constraint(equalTo: suggestionsScroll.topAnchor),
            suggestionsStack.leadingAnchor.constraint(equalTo: suggestionsScroll.leadingAnchor),
            suggestionsStack.trailingAnchor.constraint(equalTo: suggestionsScroll.trailingAnchor),
            suggestionsStack.widthAnchor.constraint(equalTo: suggestionsScroll.widthAnchor)
        ])
        mainStack.addArrangedSubview(suggestionsScroll)
    }
    
    private var selectedMode = "smooth"
    
    @objc private func modeSelected(_ sender: UIButton) {
        let modes = ["smooth", "funny", "bold", "flirty", "savage", "sweet"]
        guard sender.tag < modes.count else { return }
        selectedMode = modes[sender.tag]
        
        if let modeStack = sender.superview as? UIStackView {
            for case let btn as UIButton in modeStack.arrangedSubviews {
                btn.isSelected = false
                btn.backgroundColor = cardColor
                btn.layer.borderColor = UIColor.white.withAlphaComponent(0.06).cgColor
            }
        }
        sender.isSelected = true
        sender.backgroundColor = accentColor.withAlphaComponent(0.15)
        sender.layer.borderColor = accentColor.withAlphaComponent(0.3).cgColor
    }
    
    @objc private func switchKeyboard() {
        advanceToNextInputMode()
    }
    
    @objc private func generateTapped() {
        guard !isLoading else { return }
        let context = inputField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if context.isEmpty {
            statusLabel.text = "⚠️ Enter their message first"
            return
        }
        generateReplies(context: context, style: selectedMode)
    }
    
    private func generateReplies(context: String, style: String) {
        isLoading = true
        statusLabel.text = "✨ Generating \(style) replies..."
        generateButton.isEnabled = false
        
        for view in suggestionsStack.arrangedSubviews { view.removeFromSuperview() }
        
        let sysPrompt = """
        You generate text message replies. Style: \(style). Rules:
        1. Type like a real person: lowercase, casual, use "haha" "lol" "ngl" naturally
        2. Keep it short: 5-20 words per reply
        3. Sound human, NOT AI
        4. Give exactly 5 different replies, numbered 1-5
        5. Each reply on its own line
        """
        
        let messages: [[String: String]] = [
            ["role": "system", "content": sysPrompt],
            ["role": "user", "content": "Generate 5 \(style) replies to: \"\(context)\""]
        ]
        
        let body: [String: Any] = [
            "model": "llama-3.3-70b-versatile",
            "messages": messages,
            "temperature": 1.0,
            "max_tokens": 512
        ]
        
        guard let url = URL(string: "https://api.groq.com/openai/v1/chat/completions"),
              let jsonData = try? JSONSerialization.data(withJSONObject: body) else {
            self.statusLabel.text = "❌ Error preparing request"
            self.isLoading = false
            self.generateButton.isEnabled = true
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(groqKey)", forHTTPHeaderField: "Authorization")
        request.httpBody = jsonData
        request.timeoutInterval = 15
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.isLoading = false
                self.generateButton.isEnabled = true
                
                if let error = error {
                    self.statusLabel.text = "❌ \(error.localizedDescription)"
                    return
                }
                
                guard let data = data,
                      let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                      let choices = json["choices"] as? [[String: Any]],
                      let first = choices.first,
                      let message = first["message"] as? [String: Any],
                      let content = message["content"] as? String else {
                    self.statusLabel.text = "❌ Failed to parse response"
                    return
                }
                
                let replies = content.components(separatedBy: "\n")
                    .map { line in
                        var clean = line.trimmingCharacters(in: .whitespacesAndNewlines)
                        if let range = clean.range(of: #"^\d+[.)\-:\s]+"#, options: .regularExpression) {
                            clean = String(clean[range.upperBound...])
                        }
                        clean = clean.trimmingCharacters(in: CharacterSet(charactersIn: "\""))
                        return clean
                    }
                    .filter { $0.count > 3 && !$0.hasPrefix("#") && !$0.hasPrefix("*") }
                
                if replies.isEmpty {
                    self.statusLabel.text = "❌ No replies generated. Try again!"
                    return
                }
                
                self.statusLabel.text = "Tap a reply to type it ↓"
                self.showReplies(replies)
            }
        }.resume()
    }
    
    private func showReplies(_ replies: [String]) {
        for view in suggestionsStack.arrangedSubviews { view.removeFromSuperview() }
        
        for (i, reply) in replies.prefix(5).enumerated() {
            let btn = UIButton(type: .system)
            btn.setTitle(reply, for: .normal)
            btn.titleLabel?.font = .systemFont(ofSize: 13, weight: .medium)
            btn.titleLabel?.numberOfLines = 2
            btn.titleLabel?.lineBreakMode = .byTruncatingTail
            btn.setTitleColor(textColor, for: .normal)
            btn.backgroundColor = cardColor
            btn.layer.cornerRadius = 10
            btn.layer.borderWidth = 1
            btn.layer.borderColor = accentColor.withAlphaComponent(0.15).cgColor
            btn.contentEdgeInsets = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12)
            btn.contentHorizontalAlignment = .left
            btn.tag = i
            btn.addTarget(self, action: #selector(replyTapped(_:)), for: .touchUpInside)
            suggestionsStack.addArrangedSubview(btn)
        }
    }
    
    @objc private func replyTapped(_ sender: UIButton) {
        guard let text = sender.title(for: .normal) else { return }
        textDocumentProxy.insertText(text)
        
        UIView.animate(withDuration: 0.1, animations: {
            sender.transform = CGAffineTransform(scaleX: 0.95, y: 0.95)
            sender.backgroundColor = self.accentColor.withAlphaComponent(0.2)
        }) { _ in
            UIView.animate(withDuration: 0.1) {
                sender.transform = .identity
                sender.backgroundColor = self.cardColor
            }
        }
        
        statusLabel.text = "✅ Typed! Tap send in the app"
    }
}
